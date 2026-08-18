package br.com.codenest.agro.raiox.checklist;

import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.raiox.checklist.dto.ChecklistItemRequest;
import br.com.codenest.agro.raiox.checklist.dto.ChecklistItemResponse;
import br.com.codenest.agro.raiox.checklist.service.ChecklistItemService;
import br.com.codenest.agro.shared.Autorizacao;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

/** Checklist de custos do Raio-X, por produtor. */
@Path("/raiox/checklist")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChecklistItemResource {

    @Inject
    ChecklistItemService checklistItemService;

    @GET
    @RolesAllowed({Permissao.Codigos.RAIOXCHECKLIST_LISTAR, Autorizacao.SUPER_ADMIN})
    public PaginaResposta<ChecklistItemResponse> listar(
            @QueryParam("produtorId") UUID produtorId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("10") int limit,
            @QueryParam("sort") String sort,
            @QueryParam("termo") String termo) {
        return checklistItemService.listar(new ParametrosPaginacao(offset, limit, sort), termo, produtorId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXCHECKLIST_LISTAR, Autorizacao.SUPER_ADMIN})
    public ChecklistItemResponse buscarPorId(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return checklistItemService.buscarPorId(id, produtorId);
    }

    @POST
    @RolesAllowed({Permissao.Codigos.RAIOXCHECKLIST_INSERIR, Autorizacao.SUPER_ADMIN})
    public Response criar(@Valid ChecklistItemRequest request) {
        ChecklistItemResponse response = checklistItemService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXCHECKLIST_EDITAR, Autorizacao.SUPER_ADMIN})
    public ChecklistItemResponse editar(
            @PathParam("id") UUID id,
            @Valid ChecklistItemRequest request,
            @QueryParam("produtorId") UUID produtorId) {
        return checklistItemService.editar(id, request, produtorId);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXCHECKLIST_EXCLUIR, Autorizacao.SUPER_ADMIN})
    public Response excluir(@PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        checklistItemService.excluir(id, produtorId);
        return Response.noContent().build();
    }
}
