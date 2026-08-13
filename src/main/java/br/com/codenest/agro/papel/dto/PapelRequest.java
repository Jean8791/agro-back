package br.com.codenest.agro.papel.dto;

import br.com.codenest.agro.papel.enums.Permissao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

/**
 * O papel como a tela o monta: um nome e as caixas marcadas na matriz.
 *
 * <p>O conjunto pode vir vazio — papel que não permite nada é legítimo, e é o
 * ponto de partida de quem vai marcando aos poucos.
 */
public record PapelRequest(
        @NotBlank @Size(max = 60) String nome,
        @Size(max = 200) String descricao,
        @NotNull Set<Permissao> permissoes,
        UUID produtorId) {
}
