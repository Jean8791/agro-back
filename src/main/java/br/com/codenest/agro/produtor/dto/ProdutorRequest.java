package br.com.codenest.agro.produtor.dto;

import br.com.codenest.agro.produtor.enums.TipoPessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProdutorRequest(
        @NotBlank String nome,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{14}", message = "Informe CPF (11) ou CNPJ (14) dígitos")
                String documento,
        @NotNull TipoPessoa tipoPessoa) {
}
