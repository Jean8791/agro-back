package br.com.codenest.agro.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UsuarioUpdateRequest(
        @NotBlank String nome, @NotBlank @Email String email, @NotNull UUID papelId) {
}
