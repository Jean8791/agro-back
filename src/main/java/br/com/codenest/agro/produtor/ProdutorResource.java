package br.com.codenest.agro.produtor;

import br.com.codenest.agro.produtor.dto.ProdutorRequest;
import br.com.codenest.agro.produtor.dto.ProdutorResponse;
import br.com.codenest.agro.shared.Autorizacao;
import br.com.codenest.agro.produtor.service.ProdutorService;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

/** Cadastro de clientes — exclusivo do super admin. */
@Path("/produtores")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(Autorizacao.SUPER_ADMIN)
public class ProdutorResource {

    @Inject
    ProdutorService produtorService;

    @POST
    public Response criar(@Valid ProdutorRequest request) {
        ProdutorResponse response = produtorService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public PaginaResposta<ProdutorResponse> listar(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("10") int limit,
            @QueryParam("sort") String sort,
            @QueryParam("termo") String termo) {
        return produtorService.listar(new ParametrosPaginacao(offset, limit, sort), termo);
    }

    @GET
    @Path("/{id}")
    public ProdutorResponse buscarPorId(@PathParam("id") UUID id) {
        return produtorService.buscarPorId(id);
    }

    @PUT
    @Path("/{id}")
    public ProdutorResponse editar(@PathParam("id") UUID id, @Valid ProdutorRequest request) {
        return produtorService.editar(id, request);
    }

    @PATCH
    @Path("/{id}/inativar")
    public ProdutorResponse inativar(@PathParam("id") UUID id) {
        return produtorService.inativar(id);
    }

    @PATCH
    @Path("/{id}/reativar")
    public ProdutorResponse reativar(@PathParam("id") UUID id) {
        return produtorService.reativar(id);
    }
}
