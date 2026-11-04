package br.com.codenest.agro.raiox.simulacao.calculo;

import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Custos do ciclo ou período. Os dez primeiros são fixos do ciclo; o
 * {@code variavelPorUnidade} é multiplicado pela produção comercial.
 */
public record Custos(
        @Digits(integer = 13, fraction = 6) BigDecimal insumos,
        @Digits(integer = 13, fraction = 6) BigDecimal aquisicao,
        @Digits(integer = 13, fraction = 6) BigDecimal maoDeObra,
        @Digits(integer = 13, fraction = 6) BigDecimal energia,
        @Digits(integer = 13, fraction = 6) BigDecimal sanidade,
        @Digits(integer = 13, fraction = 6) BigDecimal servicos,
        @Digits(integer = 13, fraction = 6) BigDecimal estrutura,
        @Digits(integer = 13, fraction = 6) BigDecimal embalagem,
        @Digits(integer = 13, fraction = 6) BigDecimal frete,
        @Digits(integer = 13, fraction = 6) BigDecimal outros,
        @Digits(integer = 13, fraction = 6) BigDecimal variavelPorUnidade) {

    public List<BigDecimal> fixos() {
        return Arrays.asList(
                insumos, aquisicao, maoDeObra, energia, sanidade, servicos, estrutura, embalagem, frete, outros);
    }

    public List<BigDecimal> todos() {
        return Arrays.asList(
                insumos,
                aquisicao,
                maoDeObra,
                energia,
                sanidade,
                servicos,
                estrutura,
                embalagem,
                frete,
                outros,
                variavelPorUnidade);
    }
}
