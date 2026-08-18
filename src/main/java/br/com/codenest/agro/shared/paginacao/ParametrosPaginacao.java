package br.com.codenest.agro.shared.paginacao;

/**
 * O que cada Resource monta a partir de {@code offset}/{@code limit}/{@code
 * sort} e repassa ao Service/Repository. Mesmos nomes que o `ParametrosGrid`
 * do front manda.
 */
public record ParametrosPaginacao(int offset, int limit, String sort) {

    private static final int LIMIT_PADRAO = 10;

    /** Evita que `limit` gigante devolva a "lista inteira" de volta. */
    private static final int LIMIT_MAXIMO = 200;

    public int offsetEfetivo() {
        return Math.max(offset, 0);
    }

    public int limiteEfetivo() {
        return limit <= 0 ? LIMIT_PADRAO : Math.min(limit, LIMIT_MAXIMO);
    }
}
