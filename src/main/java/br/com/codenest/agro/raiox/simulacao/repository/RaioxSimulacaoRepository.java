package br.com.codenest.agro.raiox.simulacao.repository;

import br.com.codenest.agro.raiox.simulacao.entity.RaioxSimulacao;
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
public class RaioxSimulacaoRepository implements PanacheRepositoryBase<RaioxSimulacao, UUID> {

    private static final Map<String, String> CAMPOS_ORDENAVEIS = Map.of("nome", "nome");

    /** Mais recente primeiro só quando ninguém pediu outra ordenação. */
    private static final Sort ORDENACAO_PADRAO = Sort.by("criadoEm", Sort.Direction.Descending);

    public Optional<RaioxSimulacao> buscarDoProdutor(UUID id, UUID produtorId) {
        return find("id = ?1 and produtorId = ?2", id, produtorId).firstResultOptional();
    }

    public PaginaResposta<RaioxSimulacao> paginar(
            UUID produtorId, ParametrosPaginacao paginacao, String termo) {
        Sort sort = Ordenacao.resolver(paginacao.sort(), CAMPOS_ORDENAVEIS, ORDENACAO_PADRAO);
        PanacheQuery<RaioxSimulacao> query =
                (termo == null || termo.isBlank())
                        ? find("produtorId", sort, produtorId)
                        : find(
                                "produtorId = ?1 and (lower(nome) like ?2 or lower(produto) like ?2)",
                                sort,
                                produtorId,
                                "%" + termo.toLowerCase() + "%");
        return Paginador.paginar(query, paginacao);
    }
}
