package br.com.codenest.agro.raiox.checklist.repository;

import br.com.codenest.agro.raiox.checklist.entity.RaioxChecklistItem;
import br.com.codenest.agro.raiox.checklist.enums.ModuloChecklist;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RaioxChecklistItemRepository implements PanacheRepositoryBase<RaioxChecklistItem, UUID> {

    public List<RaioxChecklistItem> listarDoProdutor(UUID produtorId) {
        return list("produtorId", produtorId);
    }

    public Optional<RaioxChecklistItem> buscarDoProdutor(UUID id, UUID produtorId) {
        return find("id = ?1 and produtorId = ?2", id, produtorId).firstResultOptional();
    }

    /**
     * O mesmo rótulo existe em módulos diferentes de propósito (ex.:
     * "Embalagem" em Vegetal e em Animal, com notas diferentes) — por isso a
     * checagem de duplicata do semear padrão entra o módulo também.
     */
    public boolean existePorModuloERotulo(UUID produtorId, ModuloChecklist modulo, String rotulo) {
        return count("produtorId = ?1 and modulo = ?2 and rotulo = ?3", produtorId, modulo, rotulo) > 0;
    }
}
