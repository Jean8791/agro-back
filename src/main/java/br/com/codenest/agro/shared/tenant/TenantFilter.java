package br.com.codenest.agro.shared.tenant;

import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.shared.AuditContext;
import br.com.codenest.agro.shared.BaseTenantEntity;
import br.com.codenest.agro.shared.exception.ErrorResponse;
import br.com.codenest.agro.usuario.entity.Usuario;
import br.com.codenest.agro.usuario.repository.UsuarioRepository;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.Session;

/**
 * Resolve quem está falando com a API e liga o isolamento por produtor.
 *
 * <p>Roda antes de qualquer Resource. Depois dele, toda consulta a uma
 * {@link BaseTenantEntity} já sai restrita ao produtor da sessão — nenhuma
 * query precisa lembrar de filtrar, que é o único jeito de garantir que um
 * produtor nunca enxergue o dado de outro.
 *
 * <p>Super admin (usuário do produto) passa sem filtro nenhum: é ele quem
 * cadastra e dá manutenção nos produtores.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
@Transactional
public class TenantFilter implements ContainerRequestFilter {

    private static final String PREFIXO_AUTH = "auth";

    @Inject
    AuditContext auditContext;

    @Inject
    TenantContext tenantContext;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    ProdutorRepository produtorRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

    @Inject
    EntityManager entityManager;

    @Override
    public void filter(ContainerRequestContext contexto) {
        // login, refresh e logout acontecem antes de existir sessão
        if (contexto.getUriInfo().getPath().startsWith(PREFIXO_AUTH)) {
            return;
        }

        var usuarioId = auditContext.usuarioAutenticadoId().orElse(null);
        if (usuarioId == null) {
            // sem token: quem barra é o @RolesAllowed do endpoint
            return;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null || !usuario.isAtivo()) {
            recusar(contexto, "Usuário inexistente ou bloqueado");
            return;
        }

        tenantContext.setUsuarioId(usuarioId);
        tenantContext.setSuperAdmin(usuario.isAdmin());

        if (usuario.isAdmin()) {
            return;
        }

        var produtorId = auditContext.produtorId().orElse(null);
        if (produtorId == null) {
            recusar(contexto, "Token sem produtor");
            return;
        }

        Produtor produtor = produtorRepository.findById(produtorId);
        if (produtor == null || !produtor.isAtivo()) {
            recusar(contexto, "Produtor inexistente ou inativo");
            return;
        }

        if (produtorUsuarioRepository.buscarAtivo(produtorId, usuarioId).isEmpty()) {
            recusar(contexto, "Você não tem acesso a este produtor");
            return;
        }

        tenantContext.setProdutorId(produtorId);
        habilitarFiltro(produtorId);
    }

    private void habilitarFiltro(UUID produtorId) {
        entityManager
                .unwrap(Session.class)
                .enableFilter(BaseTenantEntity.FILTRO)
                .setParameter(BaseTenantEntity.PARAM_PRODUTOR, produtorId);
    }

    private void recusar(ContainerRequestContext contexto, String mensagem) {
        var corpo =
                new ErrorResponse(
                        Instant.now(),
                        Response.Status.FORBIDDEN.getStatusCode(),
                        "Forbidden",
                        mensagem,
                        contexto.getUriInfo().getPath());

        contexto.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(corpo)
                        .build());
    }
}
