package br.com.codenest.agro.raiox.checklist.entity;

import br.com.codenest.agro.raiox.checklist.enums.ModuloChecklist;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/** Um item do checklist de custos que a calculadora mostra como apoio. */
@Entity
@Table(name = "raiox_checklist_item")
public class RaioxChecklistItem extends BaseEntity {

    @Column(name = "produtor_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID produtorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ModuloChecklist modulo;

    @Column(nullable = false, length = 160)
    private String rotulo;

    @Column(length = 300)
    private String nota;

    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }

    public ModuloChecklist getModulo() {
        return modulo;
    }

    public void setModulo(ModuloChecklist modulo) {
        this.modulo = modulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
