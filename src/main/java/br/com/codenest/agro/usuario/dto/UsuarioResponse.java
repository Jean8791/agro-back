package br.com.codenest.agro.usuario.dto;

import br.com.codenest.agro.produtor.enums.Papel;
import br.com.codenest.agro.usuario.entity.Usuario;
import java.util.UUID;

public record UsuarioResponse(
        UUID id, String nome, String email, boolean ativo, boolean admin, Papel papel) {

    public static UsuarioResponse from(Usuario usuario, Papel papel) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.isAdmin(),
                papel);
    }
}
