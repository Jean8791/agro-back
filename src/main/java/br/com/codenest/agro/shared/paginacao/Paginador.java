package br.com.codenest.agro.shared.paginacao;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.List;

/**
 * Executa uma {@link PanacheQuery} já filtrada/ordenada e devolve a página
 * pedida. Usa {@code range} em vez de {@code page} porque não exige que o
 * offset seja múltiplo do limit.
 */
public final class Paginador {

    private Paginador() {}

    public static <T> PaginaResposta<T> paginar(PanacheQuery<T> query, ParametrosPaginacao parametros) {
        long total = query.count();
        int offset = parametros.offsetEfetivo();
        int limit = parametros.limiteEfetivo();
        List<T> conteudo = total == 0 ? List.of() : query.range(offset, offset + limit - 1).list();
        return PaginaResposta.de(conteudo, total);
    }
}
