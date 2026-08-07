package br.com.codenest.agro.shared;

import io.quarkus.arc.Arc;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Base de toda entidade: id, marcas de tempo e de autoria.
 *
 * <p>O id é UUID gerado por tempo ({@code Style.TIME} do Hibernate), com os
 * bits mais significativos do relógio na frente. Ids criados em sequência
 * ficam próximos no índice, evitando a fragmentação que o UUID aleatório
 * (v4) causa em chave primária.
 *
 * <p>Entidades que pertencem a um produtor estendem {@link BaseTenantEntity},
 * não esta.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "criado_por_id", updatable = false)
    private UUID criadoPorId;

    @Column(name = "atualizado_por_id")
    private UUID atualizadoPorId;

    @PrePersist
    private void marcarCriacao() {
        UUID usuarioId = usuarioAutenticadoId();
        this.criadoPorId = usuarioId;
        this.atualizadoPorId = usuarioId;
    }

    @PreUpdate
    private void marcarAtualizacao() {
        this.atualizadoPorId = usuarioAutenticadoId();
    }

    private static UUID usuarioAutenticadoId() {
        try (var handle = Arc.container().instance(AuditContext.class)) {
            return handle.get().usuarioAutenticadoId().orElse(null);
        }
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public UUID getCriadoPorId() {
        return criadoPorId;
    }

    public UUID getAtualizadoPorId() {
        return atualizadoPorId;
    }
}
