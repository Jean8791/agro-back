package br.com.codenest.agro.raiox.checklist.dto;

import br.com.codenest.agro.raiox.checklist.entity.RaioxChecklistItem;
import br.com.codenest.agro.raiox.checklist.enums.ModuloChecklist;
import java.util.UUID;

public record ChecklistItemResponse(UUID id, ModuloChecklist modulo, String rotulo, String nota) {

    public static ChecklistItemResponse from(RaioxChecklistItem entidade) {
        return new ChecklistItemResponse(
                entidade.getId(), entidade.getModulo(), entidade.getRotulo(), entidade.getNota());
    }
}
