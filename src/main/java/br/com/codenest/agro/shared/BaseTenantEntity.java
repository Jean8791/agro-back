package br.com.codenest.agro.shared;

import br.com.codenest.agro.shared.tenant.TenantContext;
import io.quarkus.arc.Arc;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Base das entidades que pertencem a um produtor (dado de cliente).
 *
 * <p>O filtro {@code produtorFilter} é ligado a cada requisição autenticada
 * pelo {@code TenantFilter}, então toda consulta a estas entidades já sai
 * restrita ao produtor da sessão — sem depender de alguém lembrar de
 * filtrar. O {@code produtorId} também é gravado sozinho no
 * {@code @PrePersist}.
 *
 * <p>Entidades globais (produtor, usuário, plano, configuração) estendem
 * {@link BaseEntity} e ficam de fora do filtro.
 */
@MappedSuperclass
@FilterDef(
        name = BaseTenantEntity.FILTRO,
        parameters = @ParamDef(name = BaseTenantEntity.PARAM_PRODUTOR, type = UUID.class))
@Filter(name = BaseTenantEntity.FILTRO, condition = "produtor_id = :produtorId")
public abstract class BaseTenantEntity extends BaseEntity {

    public static final String FILTRO = "produtorFilter";
    public static final String PARAM_PRODUTOR = "produtorId";

    @Column(name = "produtor_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID produtorId;

    @PrePersist
    private void marcarProdutor() {
        if (this.produtorId == null) {
            this.produtorId = produtorDaSessao();
        }
    }

    private static UUID produtorDaSessao() {
        try (var handle = Arc.container().instance(TenantContext.class)) {
            return handle.get().produtorIdObrigatorio();
        }
    }

    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }
}
