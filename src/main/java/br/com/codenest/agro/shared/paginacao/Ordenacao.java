package br.com.codenest.agro.shared.paginacao;

import br.com.codenest.agro.shared.exception.NegocioException;
import io.quarkus.panache.common.Sort;
import java.util.Map;

/**
 * Resolve o `sort` que o cliente manda (formato {@code "campo,asc"} ou
 * {@code "campo,desc"}) contra uma whitelist de campos por domínio — nunca
 * repassa o nome do campo direto pra query, pra não abrir brecha de injeção
 * via nome de coluna.
 */
public final class Ordenacao {

    private Ordenacao() {}

    public static Sort resolver(String sort, Map<String, String> camposPermitidos, Sort padrao) {
        if (sort == null || sort.isBlank()) {
            return padrao;
        }

        String[] partes = sort.split(",", 2);
        String coluna = camposPermitidos.get(partes[0]);
        if (coluna == null) {
            throw new NegocioException("Campo de ordenação inválido: " + partes[0]);
        }

        boolean decrescente = partes.length > 1 && "desc".equalsIgnoreCase(partes[1]);
        return decrescente ? Sort.by(coluna, Sort.Direction.Descending) : Sort.by(coluna);
    }

    /**
     * Como {@link #resolver}, mas com uma coluna fixa na frente que o cliente
     * nunca escolhe (ex: papel de sistema sempre primeiro). A coluna pedida
     * pelo cliente — ou {@code colunaPadrao}, na ausência de {@code sort} —
     * entra como critério de desempate.
     *
     * <p>Monta um {@link Sort} novo a cada chamada: {@code Sort.and(...)}
     * muta a instância em vez de devolver outra, então reaproveitar uma
     * constante {@code static final} aqui acumularia colunas a cada
     * requisição.
     */
    public static Sort resolverComPrefixo(
            String sort,
            Map<String, String> camposPermitidos,
            String colunaPrefixo,
            Sort.Direction direcaoPrefixo,
            String colunaPadrao) {
        Sort resultado = Sort.by(colunaPrefixo, direcaoPrefixo);

        if (sort == null || sort.isBlank()) {
            return resultado.and(colunaPadrao);
        }

        String[] partes = sort.split(",", 2);
        String coluna = camposPermitidos.get(partes[0]);
        if (coluna == null) {
            throw new NegocioException("Campo de ordenação inválido: " + partes[0]);
        }

        boolean decrescente = partes.length > 1 && "desc".equalsIgnoreCase(partes[1]);
        return decrescente ? resultado.and(coluna, Sort.Direction.Descending) : resultado.and(coluna);
    }
}
