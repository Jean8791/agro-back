package br.com.codenest.agro.raiox.checklist.repository;

import br.com.codenest.agro.raiox.checklist.entity.RaioxChecklistItem;
import br.com.codenest.agro.raiox.checklist.enums.ModuloChecklist;
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
public class RaioxChecklistItemRepository implements PanacheRepositoryBase<RaioxChecklistItem, UUID> {

    private static final Map<String, String> CAMPOS_ORDENAVEIS = Map.of("rotulo", "rotulo");

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

    /**
     * Módulo continua agrupando a listagem — só que agora como prefixo fixo do
     * `ORDER BY` (alfabético: AMBOS, ANIMAL, VEGETAL), não mais comparator em
     * memória. O `rotulo` é o único campo que o cliente pode escolher ordenar.
     */
    public PaginaResposta<RaioxChecklistItem> paginar(
            UUID produtorId, ParametrosPaginacao paginacao, String termo) {
        Sort sort =
                Ordenacao.resolverComPrefixo(
                        paginacao.sort(), CAMPOS_ORDENAVEIS, "modulo", Sort.Direction.Ascending, "rotulo");
        PanacheQuery<RaioxChecklistItem> query =
                (termo == null || termo.isBlank())
                        ? find("produtorId", sort, produtorId)
                        : find(
                                "produtorId = ?1 and lower(rotulo) like ?2",
                                sort,
                                produtorId,
                                "%" + termo.toLowerCase() + "%");
        return Paginador.paginar(query, paginacao);
    }
}
