package br.com.codenest.agro.raiox.simulacao;

import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.raiox.simulacao.dto.SimulacaoRequest;
import br.com.codenest.agro.raiox.simulacao.dto.SimulacaoResponse;
import br.com.codenest.agro.raiox.simulacao.service.SimulacaoService;
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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

/** Simulações salvas da calculadora — sem PUT, é imutável (ver SimulacaoService). */
@Path("/raiox/simulacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SimulacaoResource {

    @Inject
    SimulacaoService simulacaoService;

    @GET
    @RolesAllowed({Permissao.Codigos.RAIOXSIMULACAO_LISTAR, Autorizacao.SUPER_ADMIN})
    public PaginaResposta<SimulacaoResponse> listar(
            @QueryParam("produtorId") UUID produtorId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("10") int limit,
            @QueryParam("sort") String sort,
            @QueryParam("termo") String termo) {
        return simulacaoService.listar(new ParametrosPaginacao(offset, limit, sort), termo, produtorId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXSIMULACAO_LISTAR, Autorizacao.SUPER_ADMIN})
    public SimulacaoResponse buscarPorId(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return simulacaoService.buscarPorId(id, produtorId);
    }

    @POST
    @RolesAllowed({Permissao.Codigos.RAIOXSIMULACAO_INSERIR, Autorizacao.SUPER_ADMIN})
    public Response criar(@Valid SimulacaoRequest request) {
        SimulacaoResponse response = simulacaoService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXSIMULACAO_EXCLUIR, Autorizacao.SUPER_ADMIN})
    public Response excluir(@PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        simulacaoService.excluir(id, produtorId);
        return Response.noContent().build();
    }
}
