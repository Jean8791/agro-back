package br.com.codenest.agro.produtor.repository;

import br.com.codenest.agro.produtor.entity.Produtor;
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
public class ProdutorRepository implements PanacheRepositoryBase<Produtor, UUID> {

    private static final Map<String, String> CAMPOS_ORDENAVEIS = Map.of("nome", "nome");
    private static final Sort ORDENACAO_PADRAO = Sort.by("nome");

    public Optional<Produtor> findByDocumento(String documento) {
        return find("documento", documento).firstResultOptional();
    }

    public Optional<Produtor> findByDocumentoAndIdNot(String documento, UUID id) {
        return find("documento = ?1 and id != ?2", documento, id).firstResultOptional();
    }

    public PaginaResposta<Produtor> paginar(ParametrosPaginacao paginacao, String termo) {
        Sort sort = Ordenacao.resolver(paginacao.sort(), CAMPOS_ORDENAVEIS, ORDENACAO_PADRAO);
        PanacheQuery<Produtor> query =
                (termo == null || termo.isBlank())
                        ? findAll(sort)
                        : find(
                                "lower(nome) like ?1 or documento like ?1",
                                sort,
                                "%" + termo.toLowerCase() + "%");
        return Paginador.paginar(query, paginacao);
    }
}
