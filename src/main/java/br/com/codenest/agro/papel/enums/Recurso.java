package br.com.codenest.agro.papel.enums;

/**
 * Aquilo sobre o que se age — uma linha da matriz de permissões da tela de
 * papéis. Cada tela nova do sistema acrescenta um valor aqui.
 */
public enum Recurso {
    USUARIO("Usuários"),
    PAPEL("Papéis"),
    // nome sem "_" de propósito: Permissao.Codigos monta o código separando
    // NOME_ACAO no primeiro "_", e o front (codigoDaPermissao) faz o mesmo
    // split — um recurso composto quebraria os dois lados
    RAIOXPRODUTO("Raio-X · Produtos"),
    RAIOXCHECKLIST("Raio-X · Checklist de custos"),
    RAIOXSIMULACAO("Raio-X · Simulações");

    private final String rotulo;

    Recurso(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
