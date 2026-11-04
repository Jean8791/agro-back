package br.com.codenest.agro.raiox.simulacao.calculo;

import br.com.codenest.agro.raiox.simulacao.enums.Semaforo;
import java.math.BigDecimal;
import java.util.List;

/**
 * Tudo que a tela mostra depois do cálculo. Os valores ficam {@code null}
 * enquanto faltar dado para chegar neles, e {@code erros} diz o que falta.
 */
public record ResultadoCalculo(
        BigDecimal producaoTecnicaBruta,
        BigDecimal producaoTecnicaVendavel,
        BigDecimal producaoComercial,
        BigDecimal custoBase,
        BigDecimal valorReserva,
        BigDecimal custoTotal,
        BigDecimal precoMinimo,
        BigDecimal precoAlvo,
        BigDecimal receitaEsperada,
        BigDecimal resultadoEstimado,
        BigDecimal margemSegurancaReal,
        Semaforo semaforo,
        String mensagem,
        String acao,
        List<String> erros,
        List<String> avisos) {
}
