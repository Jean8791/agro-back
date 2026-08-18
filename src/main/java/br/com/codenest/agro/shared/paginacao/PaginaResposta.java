package br.com.codenest.agro.shared.paginacao;

import java.util.List;

/** Espelha o `PaginaGrid<T>` do front — mesmos nomes de campo. */
public record PaginaResposta<T>(List<T> conteudo, long total) {

    public static <T> PaginaResposta<T> de(List<T> conteudo, long total) {
        return new PaginaResposta<>(conteudo, total);
    }
}
