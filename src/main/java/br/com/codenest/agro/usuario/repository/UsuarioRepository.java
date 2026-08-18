package br.com.codenest.agro.usuario.repository;

import br.com.codenest.agro.shared.paginacao.Ordenacao;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import br.com.codenest.agro.usuario.entity.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<Usuario, UUID> {

    private static final Map<String, String> CAMPOS_ORDENAVEIS = Map.of("nome", "u.nome");
    private static final Sort ORDENACAO_PADRAO = Sort.by("u.nome");

    public Optional<Usuario> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<Usuario> findByEmailAndIdNot(String email, UUID id) {
        return find("email = ?1 and id != ?2", email, id).firstResultOptional();
    }

    /**
     * {@code Usuario} e {@code ProdutorUsuario} não têm relação JPA mapeada
     * entre si — cada um só guarda o {@code UUID} do outro — então o join sai
     * por JPQL com raízes múltiplas (estilo ANSI-92, condição no `WHERE`), em
     * vez de um `PanacheQuery` de uma entidade só. Cada linha já vem como
     * {@code [Usuario, Papel]}, pronta para {@code UsuarioResponse.from} sem
     * consulta extra por linha — ao contrário do N+1 que isto substitui.
     *
     * <p>Não filtra {@code pu.ativo}: preserva o comportamento de hoje, que
     * também não filtra.
     */
    public PaginaResposta<Object[]> paginarDoProdutor(
            UUID produtorId, ParametrosPaginacao paginacao, String termo) {
        Sort sort = Ordenacao.resolver(paginacao.sort(), CAMPOS_ORDENAVEIS, ORDENACAO_PADRAO);
        boolean temTermo = termo != null && !termo.isBlank();

        String base =
                "from Usuario u, ProdutorUsuario pu, Papel p "
                        + "where pu.usuarioId = u.id and pu.papelId = p.id and pu.produtorId = :produtorId"
                        + (temTermo
                                ? " and (lower(u.nome) like :termo or lower(u.email) like :termo)"
                                : "");

        var contagem = getEntityManager().createQuery("select count(u) " + base, Long.class);
        contagem.setParameter("produtorId", produtorId);
        if (temTermo) {
            contagem.setParameter("termo", "%" + termo.toLowerCase() + "%");
        }
        long total = contagem.getSingleResult();

        if (total == 0) {
            return PaginaResposta.de(List.of(), 0);
        }

        var selecao =
                getEntityManager()
                        .createQuery("select u, p " + base + " order by " + ordemJpql(sort), Object[].class);
        selecao.setParameter("produtorId", produtorId);
        if (temTermo) {
            selecao.setParameter("termo", "%" + termo.toLowerCase() + "%");
        }

        List<Object[]> conteudo =
                selecao
                        .setFirstResult(paginacao.offsetEfetivo())
                        .setMaxResults(paginacao.limiteEfetivo())
                        .getResultList();

        return PaginaResposta.de(conteudo, total);
    }

    private static String ordemJpql(Sort sort) {
        return sort.getColumns().stream()
                .map(
                        coluna ->
                                coluna.getName()
                                        + (coluna.getDirection() == Sort.Direction.Descending ? " desc" : " asc"))
                .collect(Collectors.joining(", "));
    }
}
