package br.com.codenest.agro.papel.repository;

import br.com.codenest.agro.papel.entity.Papel;
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
public class PapelRepository implements PanacheRepositoryBase<Papel, UUID> {

    private static final Map<String, String> CAMPOS_ORDENAVEIS = Map.of("nome", "nome");

    /**
     * Busca no escopo de quem pergunta: papel de outro produtor não existe.
     * Papel de sistema é visível a todos.
     */
    public Optional<Papel> buscarDisponivel(UUID id, UUID produtorId) {
        return find("id = ?1 and (produtorId is null or produtorId = ?2)", id, produtorId)
                .firstResultOptional();
    }

    public Optional<Papel> buscarPorNome(String nome, UUID produtorId) {
        return find("nome = ?1 and produtorId = ?2", nome, produtorId).firstResultOptional();
    }

    /** Os de sistema sempre na frente — é a ordem que o select de papel espera. */
    public PaginaResposta<Papel> paginar(UUID produtorId, ParametrosPaginacao paginacao, String termo) {
        Sort sort =
                Ordenacao.resolverComPrefixo(
                        paginacao.sort(), CAMPOS_ORDENAVEIS, "sistema", Sort.Direction.Descending, "nome");
        PanacheQuery<Papel> query =
                (termo == null || termo.isBlank())
                        ? find("produtorId is null or produtorId = ?1", sort, produtorId)
                        : find(
                                "(produtorId is null or produtorId = ?1) and (lower(nome) like ?2 or lower(descricao) like ?2)",
                                sort,
                                produtorId,
                                "%" + termo.toLowerCase() + "%");
        return Paginador.paginar(query, paginacao);
    }
}
