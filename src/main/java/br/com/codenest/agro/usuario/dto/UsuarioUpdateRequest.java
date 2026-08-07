package br.com.codenest.agro.usuario.dto;

import br.com.codenest.agro.produtor.enums.Papel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioUpdateRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotNull Papel papel) {
}
