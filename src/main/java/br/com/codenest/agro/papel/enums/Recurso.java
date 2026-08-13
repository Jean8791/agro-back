package br.com.codenest.agro.papel.enums;

/**
 * Aquilo sobre o que se age — uma linha da matriz de permissões da tela de
 * papéis. Cada tela nova do sistema acrescenta um valor aqui.
 */
public enum Recurso {
    USUARIO("Usuários"),
    PAPEL("Papéis");

    private final String rotulo;

    Recurso(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
