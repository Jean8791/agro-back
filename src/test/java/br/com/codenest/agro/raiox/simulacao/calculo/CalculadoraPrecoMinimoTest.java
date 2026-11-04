package br.com.codenest.agro.raiox.simulacao.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import br.com.codenest.agro.raiox.simulacao.enums.Semaforo;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraPrecoMinimoTest {

    private final CalculadoraPrecoMinimo calculadora = new CalculadoraPrecoMinimo();

    private static BigDecimal n(String valor) {
        return new BigDecimal(valor);
    }

    private static void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, n(esperado).compareTo(atual), () -> "esperado " + esperado + ", veio " + atual);
    }

    private static Custos custosAlface() {
        return new Custos(
                n("950"), n("0"), n("600"), n("120"), n("0"), n("0"), n("200"), n("176"), n("220"), n("50"),
                n("0"));
    }

    private static FormularioCalculo alface(BigDecimal precoEsperado) {
        return new FormularioCalculo(
                ModuloProducao.VEGETAL,
                "Alface",
                "",
                "Brasil",
                "Feira",
                "cabeça",
                "caixa",
                n("20"),
                precoEsperado,
                n("20"),
                n("10"),
                "Comprador",
                "2026-07-11",
                n("1000"),
                null,
                n("1000"),
                n("12"),
                TipoProducaoAnimal.CONTINUA,
                null,
                null,
                null,
                null,
                null,
                custosAlface(),
                List.of());
    }

    private static FormularioCalculo loteAnimal(
            String produto,
            String unidade,
            BigDecimal quantidadeAnimais,
            BigDecimal pesoMedio,
            BigDecimal perda,
            BigDecimal precoEsperado) {
        return new FormularioCalculo(
                ModuloProducao.ANIMAL,
                produto,
                "",
                "Brasil",
                "Feira",
                unidade,
                unidade,
                n("1"),
                precoEsperado,
                n("20"),
                n("0"),
                "Comprador",
                "2026-07-11",
                n("1000"),
                null,
                n("1000"),
                n("12"),
                TipoProducaoAnimal.LOTE,
                quantidadeAnimais,
                null,
                null,
                pesoMedio,
                perda,
                custosAlface(),
                List.of());
    }

    @Test
    void reproduzOExemploValidadoDeAlface() {
        ResultadoCalculo resultado = calculadora.calcular(alface(n("70")));

        assertValor("1000", resultado.producaoTecnicaBruta());
        assertValor("880", resultado.producaoTecnicaVendavel());
        assertValor("44", resultado.producaoComercial());
        assertValor("2316", resultado.custoBase());
        assertValor("2547.6", resultado.custoTotal());
        assertValor("57.9", resultado.precoMinimo());
        assertValor("69.48", resultado.precoAlvo());
        assertValor("3080", resultado.receitaEsperada());
        assertValor("532.4", resultado.resultadoEstimado());
        assertEquals(Semaforo.GREEN, resultado.semaforo());
        assertEquals(List.of(), resultado.erros());
    }

    @Test
    void classificaComoVermelhoQuandoOPrecoNaoCobreOCusto() {
        ResultadoCalculo resultado = calculadora.calcular(alface(n("50")));

        assertEquals(Semaforo.RED, resultado.semaforo());
        assertTrue(resultado.resultadoEstimado().signum() < 0);
    }

    @Test
    void classificaComoAmareloEntrePrecoMinimoEPrecoAlvo() {
        ResultadoCalculo resultado = calculadora.calcular(alface(n("60")));

        assertEquals(Semaforo.YELLOW, resultado.semaforo());
        assertTrue(resultado.resultadoEstimado().signum() > 0);
    }

    @Test
    void calculaProducaoAnimalPorLoteComMortalidade() {
        ResultadoCalculo resultado =
                calculadora.calcular(loteAnimal("Tilápia", "kg", n("1000"), n("0.9"), n("10"), n("15")));

        assertValor("900", resultado.producaoTecnicaBruta());
        assertValor("810", resultado.producaoTecnicaVendavel());
        assertValor("810", resultado.producaoComercial());
        assertEquals(List.of(), resultado.erros());
    }

    @Test
    void bloqueiaOSemaforoQuandoFaltamDadosObrigatorios() {
        FormularioCalculo completo = alface(n("70"));
        FormularioCalculo incompleto = new FormularioCalculo(
                completo.modulo(),
                completo.produtoSelecionado(),
                completo.produtoPersonalizado(),
                completo.regiao(),
                completo.canalVenda(),
                completo.unidadeTecnica(),
                completo.unidadeComercial(),
                null,
                completo.precoEsperado(),
                completo.margemDesejadaPercent(),
                completo.reservaPercent(),
                completo.fonteCotacao(),
                completo.dataCotacao(),
                null,
                null,
                null,
                completo.perdaVegetalPercent(),
                completo.tipoProducaoAnimal(),
                null,
                null,
                null,
                null,
                null,
                completo.custos(),
                List.of());

        ResultadoCalculo resultado = calculadora.calcular(incompleto);

        assertEquals(Semaforo.FILL, resultado.semaforo());
        assertTrue(resultado.erros().size() >= 2);
        assertNull(resultado.precoMinimo());
    }

    @Test
    void calculaLoteAnimalVendidoPorCabecaSemExigirPeso() {
        ResultadoCalculo resultado =
                calculadora.calcular(loteAnimal("Bezerro", "cabeça", n("20"), null, n("5"), n("1800")));

        assertValor("20", resultado.producaoTecnicaBruta());
        assertValor("19", resultado.producaoTecnicaVendavel());
        assertValor("19", resultado.producaoComercial());
        assertEquals(List.of(), resultado.erros());
    }

    @Test
    void produtoPersonalizadoUsaONomeDigitado() {
        FormularioCalculo base = alface(n("70"));
        FormularioCalculo personalizado = new FormularioCalculo(
                base.modulo(),
                FormularioCalculo.PRODUTO_PERSONALIZADO,
                "  Rúcula hidropônica ",
                base.regiao(),
                base.canalVenda(),
                base.unidadeTecnica(),
                base.unidadeComercial(),
                base.quantidadePorUnidade(),
                base.precoEsperado(),
                base.margemDesejadaPercent(),
                base.reservaPercent(),
                base.fonteCotacao(),
                base.dataCotacao(),
                base.area(),
                base.produtividade(),
                base.producaoBrutaInformada(),
                base.perdaVegetalPercent(),
                base.tipoProducaoAnimal(),
                base.quantidadeAnimais(),
                base.producaoDiariaMedia(),
                base.diasPeriodo(),
                base.pesoMedioVenda(),
                base.perdaMortalidadeAnimalPercent(),
                base.custos(),
                List.of());

        assertEquals("Rúcula hidropônica", personalizado.nomeProduto());
    }
}
