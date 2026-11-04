package br.com.codenest.agro.raiox.simulacao.dto;

import br.com.codenest.agro.raiox.simulacao.calculo.FormularioCalculo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * O que a calculadora manda ao salvar: só o nome e o formulário. Módulo,
 * produto, semáforo, preço mínimo e o resultado completo são calculados aqui
 * no back a partir do formulário — o cliente não tem como gravar um
 * resultado que a fórmula não produziria.
 */
public record SimulacaoRequest(
        @NotBlank String nome,
        @NotNull @Valid FormularioCalculo formulario,
        UUID produtorId) {
}
