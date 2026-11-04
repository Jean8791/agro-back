package br.com.codenest.agro.raiox.simulacao.calculo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Custos do ciclo ou período. Os dez primeiros são fixos do ciclo; o
 * {@code variavelPorUnidade} é multiplicado pela produção comercial.
 */
public record Custos(
        BigDecimal insumos,
        BigDecimal aquisicao,
        BigDecimal maoDeObra,
        BigDecimal energia,
        BigDecimal sanidade,
        BigDecimal servicos,
        BigDecimal estrutura,
        BigDecimal embalagem,
        BigDecimal frete,
        BigDecimal outros,
        BigDecimal variavelPorUnidade) {

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
