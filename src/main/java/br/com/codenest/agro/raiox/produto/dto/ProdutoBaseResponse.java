package br.com.codenest.agro.raiox.produto.dto;

import br.com.codenest.agro.raiox.produto.entity.RaioxProdutoBase;
import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import java.util.UUID;

public record ProdutoBaseResponse(
        UUID id,
        ModuloProducao modulo,
        String categoria,
        String produto,
        String unidadeTecnica,
        String unidadeComercial,
        double quantidadePorUnidade,
        String unidadePreco,
        TipoProducaoAnimal tipoProducaoAnimal,
        String nota,
        String fonte) {

    public static ProdutoBaseResponse from(RaioxProdutoBase entidade) {
        return new ProdutoBaseResponse(
                entidade.getId(),
                entidade.getModulo(),
                entidade.getCategoria(),
                entidade.getProduto(),
                entidade.getUnidadeTecnica(),
                entidade.getUnidadeComercial(),
                entidade.getQuantidadePorUnidade(),
                entidade.getUnidadePreco(),
                entidade.getTipoProducaoAnimal(),
                entidade.getNota(),
                entidade.getFonte());
    }
}
