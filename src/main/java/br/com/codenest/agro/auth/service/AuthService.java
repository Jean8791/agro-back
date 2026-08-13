package br.com.codenest.agro.auth.service;

import br.com.codenest.agro.auth.dto.LoginRequest;
import br.com.codenest.agro.auth.dto.LoginResponse;
import br.com.codenest.agro.auth.dto.RefreshRequest;
import br.com.codenest.agro.auth.entity.Sessao;
import br.com.codenest.agro.auth.repository.SessaoRepository;
import br.com.codenest.agro.papel.entity.Papel;
import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.papel.repository.PapelRepository;
import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.shared.Autorizacao;
import br.com.codenest.agro.shared.ClaimsJwt;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.usuario.entity.Usuario;
import br.com.codenest.agro.usuario.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CREDENCIAIS_INVALIDAS = "Credenciais inválidas";
    private static final String SESSAO_INVALIDA = "Sessão inválida ou expirada";

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    SessaoRepository sessaoRepository;

    @Inject
    ProdutorRepository produtorRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

    @Inject
    PapelRepository papelRepository;

    @Inject
    TentativaLoginService tentativaLoginService;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "agro.auth.access-token-ttl-seconds")
    long accessTokenTtlSeconds;

    @ConfigProperty(name = "agro.auth.refresh-token-ttl-seconds")
    long refreshTokenTtlSeconds;

    @Transactional
    public LoginResponse autenticar(LoginRequest request, String ip) {
        tentativaLoginService.verificarBloqueio(request.email(), ip);

        var usuario =
                usuarioRepository
                        .findByEmail(request.email())
                        .filter(Usuario::isAtivo)
                        .filter(candidato -> BcryptUtil.matches(request.senha(), candidato.getSenha()))
                        .orElse(null);

        if (usuario == null) {
            tentativaLoginService.registrarFalha(request.email(), ip);
            throw new NegocioException(CREDENCIAIS_INVALIDAS);
        }

        tentativaLoginService.limparHistorico(request.email(), ip);

        // super admin não entra "dentro" de um produtor
        if (usuario.isAdmin()) {
            return gerarTokens(usuario, null, Set.of(Autorizacao.SUPER_ADMIN), "Super admin");
        }

        var vinculo = escolherVinculo(usuario, request.produtorId());
        var papel = papelDo(vinculo);
        return gerarTokens(
                usuario, vinculo.getProdutorId(), permissoesDe(papel), papel.getNome());
    }

    @Transactional
    public LoginResponse renovar(RefreshRequest request) {
        String hash = hash(request.refreshToken());

        var sessao = sessaoRepository.findAtivaPorHash(hash).orElseThrow(() -> new NegocioException(SESSAO_INVALIDA));

        var usuario = usuarioRepository.findById(sessao.getUsuarioId());
        if (usuario == null || !usuario.isAtivo()) {
            sessao.setRevogadaEm(LocalDateTime.now());
            throw new NegocioException(SESSAO_INVALIDA);
        }

        sessao.setRevogadaEm(LocalDateTime.now());

        if (usuario.isAdmin()) {
            return gerarTokens(usuario, null, Set.of(Autorizacao.SUPER_ADMIN), "Super admin");
        }

        // o vínculo pode ter sido desativado depois que a sessão nasceu
        var vinculo =
                produtorUsuarioRepository
                        .buscarAtivo(sessao.getProdutorId(), usuario.getId())
                        .orElseThrow(() -> new NegocioException(SESSAO_INVALIDA));

        // é aqui que uma permissão tirada do papel passa a valer: o access
        // token que já está na mão do usuário segue valendo até expirar
        var papel = papelDo(vinculo);
        return gerarTokens(
                usuario, vinculo.getProdutorId(), permissoesDe(papel), papel.getNome());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        sessaoRepository
                .findAtivaPorHash(hash(request.refreshToken()))
                .ifPresent(sessao -> sessao.setRevogadaEm(LocalDateTime.now()));
    }

    /**
     * Em qual produtor a pessoa está entrando.
     *
     * <p>Com um vínculo só, entra direto. Com vários, o front precisa informar
     * qual — entrar num escolhido por acaso seria pior do que perguntar.
     */
    private ProdutorUsuario escolherVinculo(Usuario usuario, UUID produtorInformado) {
        List<ProdutorUsuario> vinculos = produtorUsuarioRepository.listarAtivosDoUsuario(usuario.getId());

        if (vinculos.isEmpty()) {
            throw new NegocioException("Usuário sem produtor vinculado");
        }

        ProdutorUsuario vinculo;
        if (produtorInformado != null) {
            vinculo =
                    vinculos.stream()
                            .filter(candidato -> candidato.getProdutorId().equals(produtorInformado))
                            .findFirst()
                            .orElseThrow(() -> new NegocioException(CREDENCIAIS_INVALIDAS));
        } else if (vinculos.size() == 1) {
            vinculo = vinculos.get(0);
        } else {
            throw new NegocioException("Informe o produtor: este usuário tem acesso a mais de um");
        }

        Produtor produtor = produtorRepository.findById(vinculo.getProdutorId());
        if (produtor == null || !produtor.isAtivo()) {
            throw new NegocioException("Produtor inativo");
        }

        return vinculo;
    }

    /**
     * O que o papel do vínculo permite, no formato que o {@code @RolesAllowed}
     * de cada endpoint espera. Papel sem permissão nenhuma gera um token que
     * autentica mas não autoriza nada — que é o correto.
     */
    private Papel papelDo(ProdutorUsuario vinculo) {
        Papel papel = papelRepository.findById(vinculo.getPapelId());
        if (papel == null) {
            throw new NegocioException(SESSAO_INVALIDA);
        }
        return papel;
    }

    private static Set<String> permissoesDe(Papel papel) {
        return papel.getPermissoes().stream().map(Permissao::getCodigo).collect(Collectors.toSet());
    }

    /**
     * As permissões vão na claim {@code groups} de propósito: é ela que o
     * {@code @RolesAllowed} lê, então autorizar por permissão não custa
     * interceptor nem consulta ao banco a cada requisição. O preço é a
     * validade do access token — mudança de papel só vale no próximo refresh.
     */
    private LoginResponse gerarTokens(
            Usuario usuario, UUID produtorId, Set<String> permissoes, String papelNome) {
        var builder =
                Jwt.issuer(issuer)
                        .subject(usuario.getId().toString())
                        .claim(ClaimsJwt.EMAIL, usuario.getEmail())
                        .groups(permissoes)
                        .expiresIn(accessTokenTtlSeconds);

        if (papelNome != null) {
            // só para a interface ter o que escrever no cabeçalho: quem
            // autoriza são as permissões, nunca este nome
            builder = builder.claim(ClaimsJwt.PAPEL, papelNome);
        }

        if (produtorId != null) {
            builder = builder.claim(ClaimsJwt.PRODUTOR, produtorId.toString());
        }

        String accessToken = builder.sign();
        String refreshToken = novoRefreshToken(usuario.getId(), produtorId);

        return new LoginResponse(accessToken, accessTokenTtlSeconds, refreshToken, refreshTokenTtlSeconds);
    }

    private String novoRefreshToken(UUID usuarioId, UUID produtorId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        var sessao = new Sessao();
        sessao.setUsuarioId(usuarioId);
        sessao.setProdutorId(produtorId);
        sessao.setRefreshTokenHash(hash(refreshToken));
        sessao.setExpiraEm(LocalDateTime.now().plusSeconds(refreshTokenTtlSeconds));
        sessaoRepository.persist(sessao);

        return refreshToken;
    }

    private static String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
