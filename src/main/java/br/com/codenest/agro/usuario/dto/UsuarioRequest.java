package br.com.codenest.agro.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Cadastro de usuário. O `produtorId` só é informado pelo super admin, que
 * não tem produtor na sessão; para os demais ele vem do token e é ignorado
 * se vier preenchido.
 *
 * <p>O `papelId` aponta para um papel de sistema ou para um que o próprio
 * produtor criou — papel de outro produtor não é aceito.
 */
public record UsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72, message = "A senha precisa ter entre 8 e 72 caracteres") String senha,
        @NotNull UUID papelId,
        UUID produtorId) {
}
