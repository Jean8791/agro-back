package br.com.codenest.agro.produtor.entity;

import br.com.codenest.agro.produtor.enums.Papel;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

/**
 * Vínculo entre uma pessoa e um produtor, com o papel que ela exerce ali.
 *
 * <p>É N:N de propósito: a mesma pessoa pode atender mais de um produtor com
 * papéis diferentes, e desativar o vínculo não apaga o usuário.
 */
@Entity
@Table(
        name = "produtor_usuario",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_produtor_usuario",
                        columnNames = {"produtor_id", "usuario_id"}))
public class ProdutorUsuario extends BaseEntity {

    @Column(name = "produtor_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID produtorId;

    @Column(name = "usuario_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Papel papel;

    @Column(nullable = false)
    private boolean ativo = true;

    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
