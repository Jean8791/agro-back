package br.com.codenest.agro.raiox.simulacao.calculo;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        @Size(max = 120) String produtoSelecionado,
        @Size(max = 120) String produtoPersonalizado,
        @Size(max = 120) String regiao,
        @Size(max = 120) String canalVenda,
        @Size(max = 120) String unidadeTecnica,
        @Size(max = 120) String unidadeComercial,
        @Digits(integer = 13, fraction = 6) BigDecimal quantidadePorUnidade,
        @Digits(integer = 13, fraction = 6) BigDecimal precoEsperado,
        @Digits(integer = 13, fraction = 6) BigDecimal margemDesejadaPercent,
        @Digits(integer = 13, fraction = 6) BigDecimal reservaPercent,
        @Size(max = 120) String fonteCotacao,
        @Size(max = 120) String dataCotacao,
        @Digits(integer = 13, fraction = 6) BigDecimal area,
        @Digits(integer = 13, fraction = 6) BigDecimal produtividade,
        @Digits(integer = 13, fraction = 6) BigDecimal producaoBrutaInformada,
        @Digits(integer = 13, fraction = 6) BigDecimal perdaVegetalPercent,
        TipoProducaoAnimal tipoProducaoAnimal,
        @Digits(integer = 13, fraction = 6) BigDecimal quantidadeAnimais,
        @Digits(integer = 13, fraction = 6) BigDecimal producaoDiariaMedia,
        @Digits(integer = 13, fraction = 6) BigDecimal diasPeriodo,
        @Digits(integer = 13, fraction = 6) BigDecimal pesoMedioVenda,
        @Digits(integer = 13, fraction = 6) BigDecimal perdaMortalidadeAnimalPercent,
        @NotNull @Valid Custos custos,
        @Size(max = 30) List<@Size(max = 60) String> custosMarcados) {

    /** Valor do select de produto quando a pessoa digita um que não está no catálogo. */
    public static final String PRODUTO_PERSONALIZADO = "__personalizado__";

    public String nomeProduto() {
        String nome = PRODUTO_PERSONALIZADO.equals(produtoSelecionado) ? produtoPersonalizado : produtoSelecionado;
        return nome == null ? "" : nome.strip();
    }
}
