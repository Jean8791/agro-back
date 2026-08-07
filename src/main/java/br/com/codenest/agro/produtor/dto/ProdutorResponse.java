package br.com.codenest.agro.produtor.dto;

import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.enums.TipoPessoa;
import java.util.UUID;

public record ProdutorResponse(
        UUID id, String nome, String documento, TipoPessoa tipoPessoa, boolean ativo) {

    public static ProdutorResponse from(Produtor produtor) {
        return new ProdutorResponse(
                produtor.getId(),
                produtor.getNome(),
                produtor.getDocumento(),
                produtor.getTipoPessoa(),
                produtor.isAtivo());
    }
}
