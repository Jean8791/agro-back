package br.com.codenest.agro.papel;

import br.com.codenest.agro.papel.dto.PapelRequest;
import br.com.codenest.agro.papel.dto.PapelResponse;
import br.com.codenest.agro.papel.dto.RecursoResponse;
import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.papel.service.PapelService;
import br.com.codenest.agro.shared.Autorizacao;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Papéis: o que cada pessoa pode fazer dentro do produtor.
 *
 * <p>Como em `/usuarios`, o `produtorId` existe para o super admin dizer sobre
 * qual produtor está trabalhando, e é ignorado para os demais.
 */
@Path("/papeis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PapelResource {

    @Inject
    PapelService papelService;

    /**
     * A matriz de recursos e ações que a tela desenha. Basta estar autenticado
     * com permissão de ver papéis: é a definição do que existe no sistema, não
     * dado de ninguém.
     */
    @GET
    @Path("/permissoes")
    @RolesAllowed({Permissao.Codigos.PAPEL_LISTAR, Autorizacao.SUPER_ADMIN})
    public List<RecursoResponse> permissoes() {
        return RecursoResponse.matriz();
    }

    @GET
    @RolesAllowed({
        Permissao.Codigos.PAPEL_LISTAR,
        // quem cadastra usuário precisa da lista para preencher o select de papel
        Permissao.Codigos.USUARIO_INSERIR,
        Permissao.Codigos.USUARIO_EDITAR,
        Autorizacao.SUPER_ADMIN
    })
    public List<PapelResponse> listar(@QueryParam("produtorId") UUID produtorId) {
        return papelService.listar(produtorId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.PAPEL_LISTAR, Autorizacao.SUPER_ADMIN})
    public PapelResponse buscarPorId(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return papelService.buscarPorId(id, produtorId);
    }

    @POST
    @RolesAllowed({Permissao.Codigos.PAPEL_INSERIR, Autorizacao.SUPER_ADMIN})
    public Response criar(@Valid PapelRequest request) {
        PapelResponse response = papelService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.PAPEL_EDITAR, Autorizacao.SUPER_ADMIN})
    public PapelResponse editar(
            @PathParam("id") UUID id,
            @Valid PapelRequest request,
            @QueryParam("produtorId") UUID produtorId) {
        return papelService.editar(id, request, produtorId);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.PAPEL_EXCLUIR, Autorizacao.SUPER_ADMIN})
    public Response excluir(@PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        papelService.excluir(id, produtorId);
        return Response.noContent().build();
    }
}
