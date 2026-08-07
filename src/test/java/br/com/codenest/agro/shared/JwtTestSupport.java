package br.com.codenest.agro.shared;

import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.enums.Papel;
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
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Tokens para os testes, sem depender de nenhum dado semeado.
 *
 * <p>Cada token é assinado para um usuário persistido de verdade — as chaves
 * estrangeiras de auditoria exigem um id que exista. Para os tokens de
 * produtor, cria também o produtor e o vínculo, que é o que o
 * {@code TenantFilter} valida a cada requisição.
 */
@ApplicationScoped
public class JwtTestSupport {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    ProdutorRepository produtorRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

    /** Usuário do produto: sem produtor, enxerga tudo. */
    public String tokenSuperAdmin() {
        UUID usuarioId = QuarkusTransaction.requiringNew().call(() -> criarUsuario(true).getId());
        return assinar(usuarioId, Papel.SUPER_ADMIN, null);
    }

    /** Admin de um produtor novo em folha. */
    public String tokenAdmin() {
        return tokenDoPapel(Papel.ADMIN).token();
    }

    /** Usuário comum de um produtor novo em folha. */
    public String tokenUsuario() {
        return tokenDoPapel(Papel.CONSULTA).token();
    }

    /** Token com o produtor junto, para os testes que precisam do id dele. */
    public Acesso tokenDoPapel(Papel papel) {
        return QuarkusTransaction.requiringNew()
                .call(
                        () -> {
                            Usuario usuario = criarUsuario(false);
                            Produtor produtor = criarProdutor();
                            vincular(produtor, usuario, papel);
                            return new Acesso(
                                    assinar(usuario.getId(), papel.name(), produtor.getId()),
                                    produtor.getId(),
                                    usuario.getId());
                        });
    }

    /** Mais um usuário dentro de um produtor que já existe. */
    public Acesso novoAcessoNoProdutor(UUID produtorId, Papel papel) {
        return QuarkusTransaction.requiringNew()
                .call(
                        () -> {
                            Usuario usuario = criarUsuario(false);
                            Produtor produtor = produtorRepository.findById(produtorId);
                            vincular(produtor, usuario, papel);
                            return new Acesso(
                                    assinar(usuario.getId(), papel.name(), produtorId),
                                    produtorId,
                                    usuario.getId());
                        });
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

    private void vincular(Produtor produtor, Usuario usuario, Papel papel) {
        var vinculo = new ProdutorUsuario();
        vinculo.setProdutorId(produtor.getId());
        vinculo.setUsuarioId(usuario.getId());
        vinculo.setPapel(papel);
        produtorUsuarioRepository.persist(vinculo);
    }

    private String assinar(UUID usuarioId, String papel, UUID produtorId) {
        String issuer = ConfigProvider.getConfig().getValue("mp.jwt.verify.issuer", String.class);

        var builder =
                Jwt.issuer(issuer)
                        .subject(usuarioId.toString())
                        .groups(Set.of(papel))
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
