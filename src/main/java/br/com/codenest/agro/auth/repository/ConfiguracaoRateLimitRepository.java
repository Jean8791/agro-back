package br.com.codenest.agro.auth.repository;

import br.com.codenest.agro.auth.entity.ConfiguracaoRateLimit;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ConfiguracaoRateLimitRepository
        implements PanacheRepositoryBase<ConfiguracaoRateLimit, UUID> {

    /** A tabela tem uma linha só, criada pela migration. */
    public Optional<ConfiguracaoRateLimit> buscarUnica() {
        return findAll().firstResultOptional();
    }
}
