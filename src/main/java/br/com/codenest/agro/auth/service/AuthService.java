package br.com.codenest.agro.auth.service;

import br.com.codenest.agro.auth.dto.LoginRequest;
import br.com.codenest.agro.auth.dto.LoginResponse;
import br.com.codenest.agro.auth.dto.RefreshRequest;
import br.com.codenest.agro.auth.entity.Sessao;
import br.com.codenest.agro.auth.repository.SessaoRepository;
import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.enums.Papel;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
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
            return gerarTokens(usuario, null, Papel.SUPER_ADMIN);
        }

        var vinculo = escolherVinculo(usuario, request.produtorId());
        return gerarTokens(usuario, vinculo.getProdutorId(), vinculo.getPapel().name());
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
            return gerarTokens(usuario, null, Papel.SUPER_ADMIN);
        }

        // o vínculo pode ter sido desativado depois que a sessão nasceu
        var vinculo =
                produtorUsuarioRepository
                        .buscarAtivo(sessao.getProdutorId(), usuario.getId())
                        .orElseThrow(() -> new NegocioException(SESSAO_INVALIDA));

        return gerarTokens(usuario, vinculo.getProdutorId(), vinculo.getPapel().name());
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

    private LoginResponse gerarTokens(Usuario usuario, UUID produtorId, String papel) {
        var builder =
                Jwt.issuer(issuer)
                        .subject(usuario.getId().toString())
                        .claim(ClaimsJwt.EMAIL, usuario.getEmail())
                        .groups(Set.of(papel))
                        .expiresIn(accessTokenTtlSeconds);

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
