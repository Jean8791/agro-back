package br.com.codenest.agro.raiox.produto.repository;

import br.com.codenest.agro.raiox.produto.entity.RaioxProdutoBase;
import br.com.codenest.agro.shared.paginacao.Ordenacao;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.Paginador;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RaioxProdutoBaseRepository implements PanacheRepositoryBase<RaioxProdutoBase, UUID> {

    private static final Map<String, String> CAMPOS_ORDENAVEIS = Map.of("produto", "produto");
    private static final Sort ORDENACAO_PADRAO = Sort.by("produto");

    public Optional<RaioxProdutoBase> buscarDoProdutor(UUID id, UUID produtorId) {
        return find("id = ?1 and produtorId = ?2", id, produtorId).firstResultOptional();
    }

    public boolean existePorProdutoECategoria(UUID produtorId, String produto, String categoria) {
        return count("produtorId = ?1 and produto = ?2 and categoria = ?3", produtorId, produto, categoria)
                > 0;
    }

    public PaginaResposta<RaioxProdutoBase> paginar(
            UUID produtorId, ParametrosPaginacao paginacao, String termo) {
        Sort sort = Ordenacao.resolver(paginacao.sort(), CAMPOS_ORDENAVEIS, ORDENACAO_PADRAO);
        PanacheQuery<RaioxProdutoBase> query =
                (termo == null || termo.isBlank())
                        ? find("produtorId", sort, produtorId)
                        : find(
                                "produtorId = ?1 and (lower(produto) like ?2 or lower(categoria) like ?2)",
                                sort,
                                produtorId,
                                "%" + termo.toLowerCase() + "%");
        return Paginador.paginar(query, paginacao);
    }
}
