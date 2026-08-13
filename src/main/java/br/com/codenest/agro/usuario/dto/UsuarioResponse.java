package br.com.codenest.agro.usuario.dto;

import br.com.codenest.agro.papel.entity.Papel;
import br.com.codenest.agro.usuario.entity.Usuario;
import java.util.UUID;

/**
 * O papel vai como id e nome: o id é o que o formulário devolve, o nome é o
 * que a listagem mostra sem precisar de uma segunda consulta.
 */
public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        boolean ativo,
        boolean admin,
        UUID papelId,
        String papelNome) {

    public static UsuarioResponse from(Usuario usuario, Papel papel) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.isAdmin(),
                papel == null ? null : papel.getId(),
                papel == null ? null : papel.getNome());
    }
}
