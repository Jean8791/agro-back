package br.com.codenest.agro.shared.tenant;

import jakarta.enterprise.context.RequestScoped;
import java.util.UUID;

/**
 * Quem está falando com a API nesta requisição: o usuário, o produtor
 * (tenant) da sessão e se é um usuário nosso (super admin).
 *
 * <p>Preenchido pelo {@link TenantFilter} antes de qualquer Resource rodar.
 */
@RequestScoped
public class TenantContext {

    private UUID usuarioId;
    private UUID produtorId;
    private boolean superAdmin;

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    /** Nulo quando é super admin: ele não está preso a um produtor. */
    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    /**
     * Produtor da sessão, exigido. Use onde um registro precisa ser gravado
     * para um produtor — o super admin tem que escolher um antes.
     */
    public UUID produtorIdObrigatorio() {
        if (produtorId == null) {
            throw new IllegalStateException(
                    "Operação exige um produtor na sessão e o token não tem nenhum");
        }
        return produtorId;
    }
}
