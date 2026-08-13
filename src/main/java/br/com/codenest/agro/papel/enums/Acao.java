package br.com.codenest.agro.papel.enums;

/**
 * O que se faz com um recurso — uma coluna da matriz de permissões.
 *
 * <p>{@code LISTAR} é a base: sem ele o item não aparece no menu, e as outras
 * ações não têm de onde partir.
 */
public enum Acao {
    LISTAR("Ver"),
    INSERIR("Inserir"),
    EDITAR("Editar"),
    EXCLUIR("Excluir"),
    BLOQUEAR("Bloquear");

    private final String rotulo;

    Acao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
