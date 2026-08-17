package br.com.codenest.agro.raiox.produto;

import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.raiox.produto.dto.ProdutoBaseRequest;
import br.com.codenest.agro.raiox.produto.dto.ProdutoBaseResponse;
import br.com.codenest.agro.raiox.produto.service.ProdutoBaseService;
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

/** Catálogo de produtos/atividades do Raio-X, por produtor. */
@Path("/raiox/produtos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProdutoBaseResource {

    @Inject
    ProdutoBaseService produtoBaseService;

    @GET
    @RolesAllowed({Permissao.Codigos.RAIOXPRODUTO_LISTAR, Autorizacao.SUPER_ADMIN})
    public List<ProdutoBaseResponse> listar(@QueryParam("produtorId") UUID produtorId) {
        return produtoBaseService.listar(produtorId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXPRODUTO_LISTAR, Autorizacao.SUPER_ADMIN})
    public ProdutoBaseResponse buscarPorId(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return produtoBaseService.buscarPorId(id, produtorId);
    }

    @POST
    @RolesAllowed({Permissao.Codigos.RAIOXPRODUTO_INSERIR, Autorizacao.SUPER_ADMIN})
    public Response criar(@Valid ProdutoBaseRequest request) {
        ProdutoBaseResponse response = produtoBaseService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXPRODUTO_EDITAR, Autorizacao.SUPER_ADMIN})
    public ProdutoBaseResponse editar(
            @PathParam("id") UUID id,
            @Valid ProdutoBaseRequest request,
            @QueryParam("produtorId") UUID produtorId) {
        return produtoBaseService.editar(id, request, produtorId);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.RAIOXPRODUTO_EXCLUIR, Autorizacao.SUPER_ADMIN})
    public Response excluir(@PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        produtoBaseService.excluir(id, produtorId);
        return Response.noContent().build();
    }

    /** Insere o que falta do catálogo padrão (75 produtos + checklist) — idempotente. */
    @POST
    @Path("/semear-padrao")
    @RolesAllowed({Permissao.Codigos.RAIOXPRODUTO_INSERIR, Autorizacao.SUPER_ADMIN})
    public Response semearPadrao(@QueryParam("produtorId") UUID produtorId) {
        produtoBaseService.semearPadrao(produtorId);
        return Response.noContent().build();
    }
}
