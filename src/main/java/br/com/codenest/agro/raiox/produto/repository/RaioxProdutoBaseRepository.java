package br.com.codenest.agro.raiox.produto.repository;

import br.com.codenest.agro.raiox.produto.entity.RaioxProdutoBase;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RaioxProdutoBaseRepository implements PanacheRepositoryBase<RaioxProdutoBase, UUID> {

    public List<RaioxProdutoBase> listarDoProdutor(UUID produtorId) {
        return list("produtorId", produtorId);
    }

    public Optional<RaioxProdutoBase> buscarDoProdutor(UUID id, UUID produtorId) {
        return find("id = ?1 and produtorId = ?2", id, produtorId).firstResultOptional();
    }

    public boolean existePorProdutoECategoria(UUID produtorId, String produto, String categoria) {
        return count("produtorId = ?1 and produto = ?2 and categoria = ?3", produtorId, produto, categoria)
                > 0;
    }
}
