package br.com.codenest.agro.shared;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Quem está fazendo a requisição, lido do JWT. Usado pelo {@link BaseEntity}
 * para preencher autoria sem que ninguém precise se lembrar disso.
 */
@ApplicationScoped
@Unremovable
public class AuditContext {

    @Inject
    JsonWebToken jsonWebToken;

    public Optional<UUID> usuarioAutenticadoId() {
        return uuidDaClaim(jsonWebToken.getSubject());
    }

    /**
     * Produtor (tenant) do token. Vazio para o super admin, que não está
     * amarrado a nenhum — ver {@link br.com.codenest.agro.shared.tenant.TenantContext}.
     */
    public Optional<UUID> produtorId() {
        return uuidDaClaim(jsonWebToken.getClaim(ClaimsJwt.PRODUTOR));
    }

    private static Optional<UUID> uuidDaClaim(Object valor) {
        if (valor == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(valor.toString()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
