package br.com.codenest.agro.auth.repository;

import br.com.codenest.agro.auth.entity.TentativaLogin;
import br.com.codenest.agro.auth.enums.TipoTentativa;
import br.com.codenest.agro.shared.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TentativaLoginRepository implements PanacheRepositoryBase<TentativaLogin, UUID> {

    public long contarDesde(TipoTentativa tipo, String chave, LocalDateTime desde) {
        return count("tipo = ?1 and chave = ?2 and criadoEm > ?3", tipo, chave, desde);
    }

    public Optional<LocalDateTime> ultimaDesde(TipoTentativa tipo, String chave, LocalDateTime desde) {
        return find("tipo = ?1 and chave = ?2 and criadoEm > ?3 order by criadoEm desc", tipo, chave, desde)
                .firstResultOptional()
                .map(BaseEntity::getCriadoEm);
    }

    public void apagarDaChave(TipoTentativa tipo, String chave) {
        delete("tipo = ?1 and chave = ?2", tipo, chave);
    }

    public void apagarAnterioresA(TipoTentativa tipo, String chave, LocalDateTime limite) {
        delete("tipo = ?1 and chave = ?2 and criadoEm <= ?3", tipo, chave, limite);
    }
}
