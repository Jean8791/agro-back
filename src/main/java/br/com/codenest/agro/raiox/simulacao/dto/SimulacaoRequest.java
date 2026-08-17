package br.com.codenest.agro.raiox.simulacao.dto;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.simulacao.enums.Semaforo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * O que a calculadora manda ao salvar. {@code formulario} e
 * {@code resultado} vêm exatamente como {@code calculo.ts} os produziu — o
 * back não os reconstrói, só grava.
 */
public record SimulacaoRequest(
        @NotBlank String nome,
        @NotNull ModuloProducao modulo,
        @NotBlank String produto,
        @NotNull Semaforo semaforo,
        BigDecimal precoMinimo,
        @NotEmpty Map<String, Object> formulario,
        @NotEmpty Map<String, Object> resultado,
        UUID produtorId) {
}
