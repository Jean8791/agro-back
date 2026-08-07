package br.com.codenest.agro.auth.repository;

import br.com.codenest.agro.auth.entity.Sessao;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessaoRepository implements PanacheRepositoryBase<Sessao, UUID> {

    public Optional<Sessao> findAtivaPorHash(String refreshTokenHash) {
        return find("refreshTokenHash = ?1 and revogadaEm is null and expiraEm > ?2",
                refreshTokenHash, LocalDateTime.now())
                .firstResultOptional();
    }

    public void revogarTodasDoUsuario(UUID usuarioId) {
        update("revogadaEm = ?1 where usuarioId = ?2 and revogadaEm is null",
                LocalDateTime.now(), usuarioId);
    }
}
