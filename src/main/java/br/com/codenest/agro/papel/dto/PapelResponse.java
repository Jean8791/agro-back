package br.com.codenest.agro.papel.dto;

import br.com.codenest.agro.papel.entity.Papel;
import br.com.codenest.agro.papel.enums.Permissao;
import java.util.List;
import java.util.UUID;

/**
 * O `sistema` é o que a tela usa para desabilitar edição e exclusão: papel de
 * sistema pode ser usado por qualquer produtor, mas não alterado por nenhum.
 */
public record PapelResponse(
        UUID id, String nome, String descricao, boolean sistema, List<Permissao> permissoes) {

    public static PapelResponse from(Papel papel) {
        return new PapelResponse(
                papel.getId(),
                papel.getNome(),
                papel.getDescricao(),
                papel.isSistema(),
                papel.getPermissoes().stream().sorted().toList());
    }
}
