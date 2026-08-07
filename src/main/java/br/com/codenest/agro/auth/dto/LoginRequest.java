package br.com.codenest.agro.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Credenciais de acesso. O `produtorId` só é necessário quando a pessoa tem
 * vínculo ativo com mais de um produtor — aí ela precisa dizer em qual entra.
 */
public record LoginRequest(@NotBlank String email, @NotBlank String senha, UUID produtorId) {
}
