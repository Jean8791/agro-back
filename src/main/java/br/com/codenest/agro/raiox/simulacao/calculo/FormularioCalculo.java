package br.com.codenest.agro.raiox.simulacao.calculo;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * O que a tela da calculadora preenche. Campo numérico vazio chega como
 * {@code null}; quem decide se ele é obrigatório é a
 * {@link CalculadoraPrecoMinimo}, que devolve a mensagem em
 * {@link ResultadoCalculo#erros()} em vez de recusar a requisição — a tela
 * mostra o que falta enquanto a pessoa ainda está preenchendo.
 */
public record FormularioCalculo(
        @NotNull ModuloProducao modulo,
        String produtoSelecionado,
        String produtoPersonalizado,
        String regiao,
        String canalVenda,
        String unidadeTecnica,
        String unidadeComercial,
        BigDecimal quantidadePorUnidade,
        BigDecimal precoEsperado,
        BigDecimal margemDesejadaPercent,
        BigDecimal reservaPercent,
        String fonteCotacao,
        String dataCotacao,
        BigDecimal area,
        BigDecimal produtividade,
        BigDecimal producaoBrutaInformada,
        BigDecimal perdaVegetalPercent,
        TipoProducaoAnimal tipoProducaoAnimal,
        BigDecimal quantidadeAnimais,
        BigDecimal producaoDiariaMedia,
        BigDecimal diasPeriodo,
        BigDecimal pesoMedioVenda,
        BigDecimal perdaMortalidadeAnimalPercent,
        @NotNull @Valid Custos custos,
        List<String> custosMarcados) {

    /** Valor do select de produto quando a pessoa digita um que não está no catálogo. */
    public static final String PRODUTO_PERSONALIZADO = "__personalizado__";

    public String nomeProduto() {
        String nome = PRODUTO_PERSONALIZADO.equals(produtoSelecionado) ? produtoPersonalizado : produtoSelecionado;
        return nome == null ? "" : nome.strip();
    }
}
