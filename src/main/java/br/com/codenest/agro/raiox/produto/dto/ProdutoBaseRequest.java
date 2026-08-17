package br.com.codenest.agro.raiox.produto.dto;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ProdutoBaseRequest(
        @NotNull ModuloProducao modulo,
        @NotBlank String categoria,
        @NotBlank String produto,
        @NotBlank String unidadeTecnica,
        @NotBlank String unidadeComercial,
        @Positive double quantidadePorUnidade,
        @NotBlank String unidadePreco,
        TipoProducaoAnimal tipoProducaoAnimal,
        String nota,
        String fonte,
        UUID produtorId) {
}
