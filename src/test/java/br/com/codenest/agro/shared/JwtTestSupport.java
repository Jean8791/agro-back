package br.com.codenest.agro.shared;

import br.com.codenest.agro.papel.PapeisDeSistema;
import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.papel.repository.PapelRepository;
import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.enums.TipoPessoa;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.usuario.entity.Usuario;
import br.com.codenest.agro.usuario.repository.UsuarioRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Tokens para os testes, sem depender de nenhum dado semeado.
 *
 * <p>Cada token é assinado para um usuário persistido de verdade — as chaves
 * estrangeiras de auditoria exigem um id que exista. Para os tokens de
 * produtor, cria também o produtor e o vínculo, que é o que o
 * {@code TenantFilter} valida a cada requisição.
 *
 * <p>As permissões saem do papel gravado no banco, e não de uma lista escrita
 * à mão aqui: assim o token do teste é montado do mesmo jeito que o do login
 * de verdade, e um papel que perde permissão quebra o teste que dependia dela.
 */
@ApplicationScoped
public class JwtTestSupport {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    ProdutorRepository produtorRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

    @Inject
    PapelRepository papelRepository;

    /** Usuário do produto: sem produtor, enxerga tudo. */
    public String tokenSuperAdmin() {
        UUID usuarioId = QuarkusTransaction.requiringNew().call(() -> criarUsuario(true).getId());
        return assinar(usuarioId, Set.of(Autorizacao.SUPER_ADMIN), null);
    }

    /** Admin de um produtor novo em folha. */
    public String tokenAdmin() {
        return tokenDoPapel(PapeisDeSistema.ADMINISTRADOR).token();
    }

    /** Usuário comum de um produtor novo em folha: só enxerga, não altera. */
    public String tokenUsuario() {
        return tokenDoPapel(PapeisDeSistema.CONSULTA).token();
    }

    /** Token com o produtor junto, para os testes que precisam do id dele. */
    public Acesso tokenDoPapel(UUID papelId) {
        return QuarkusTransaction.requiringNew()
                .call(
                        () -> {
                            Usuario usuario = criarUsuario(false);
                            Produtor produtor = criarProdutor();
                            vincular(produtor, usuario, papelId);
                            return new Acesso(
                                    assinar(usuario.getId(), permissoes(papelId), produtor.getId()),
                                    produtor.getId(),
                                    usuario.getId());
                        });
    }

    /** Mais um usuário dentro de um produtor que já existe. */
    public Acesso novoAcessoNoProdutor(UUID produtorId, UUID papelId) {
        return QuarkusTransaction.requiringNew()
                .call(
                        () -> {
                            Usuario usuario = criarUsuario(false);
                            Produtor produtor = produtorRepository.findById(produtorId);
                            vincular(produtor, usuario, papelId);
                            return new Acesso(
                                    assinar(usuario.getId(), permissoes(papelId), produtorId),
                                    produtorId,
                                    usuario.getId());
                        });
    }

    private Set<String> permissoes(UUID papelId) {
        return papelRepository.findById(papelId).getPermissoes().stream()
                .map(Permissao::getCodigo)
                .collect(Collectors.toSet());
    }

    private Usuario criarUsuario(boolean admin) {
        var usuario = new Usuario();
        usuario.setNome("Usuário de apoio (teste)");
        usuario.setEmail("apoio." + System.nanoTime() + "@teste.com");
        usuario.setSenha("não-usado-em-login");
        usuario.setAdmin(admin);
        usuarioRepository.persist(usuario);
        return usuario;
    }

    private Produtor criarProdutor() {
        var produtor = new Produtor();
        produtor.setNome("Produtor de apoio (teste)");
        produtor.setDocumento(documentoUnico());
        produtor.setTipoPessoa(TipoPessoa.PJ);
        produtorRepository.persist(produtor);
        return produtor;
    }

    private void vincular(Produtor produtor, Usuario usuario, UUID papelId) {
        var vinculo = new ProdutorUsuario();
        vinculo.setProdutorId(produtor.getId());
        vinculo.setUsuarioId(usuario.getId());
        vinculo.setPapelId(papelId);
        produtorUsuarioRepository.persist(vinculo);
    }

    private String assinar(UUID usuarioId, Set<String> permissoes, UUID produtorId) {
        String issuer = ConfigProvider.getConfig().getValue("mp.jwt.verify.issuer", String.class);

        var builder =
                Jwt.issuer(issuer)
                        .subject(usuarioId.toString())
                        .groups(permissoes)
                        .expiresIn(3600);

        if (produtorId != null) {
            builder = builder.claim(ClaimsJwt.PRODUTOR, produtorId.toString());
        }

        return builder.sign();
    }

    /** Documento é unique: 14 dígitos aleatórios evitam colisão entre testes. */
    private static String documentoUnico() {
        long numero = ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L);
        return String.valueOf(numero);
    }

    public record Acesso(String token, UUID produtorId, UUID usuarioId) {
    }
}
