package br.com.codenest.agro.raiox.checklist.dto;

import br.com.codenest.agro.raiox.checklist.enums.ModuloChecklist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChecklistItemRequest(
        @NotNull ModuloChecklist modulo,
        @NotBlank String rotulo,
        String nota,
        UUID produtorId) {
}
