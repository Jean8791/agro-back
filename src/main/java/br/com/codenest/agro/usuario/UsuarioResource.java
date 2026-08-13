package br.com.codenest.agro.usuario;

import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.shared.Autorizacao;
import br.com.codenest.agro.usuario.dto.UsuarioRequest;
import br.com.codenest.agro.usuario.dto.UsuarioResponse;
import br.com.codenest.agro.usuario.dto.UsuarioUpdateRequest;
import br.com.codenest.agro.usuario.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import java.util.List;
import java.util.UUID;

/**
 * Usuários de um produtor.
 *
 * <p>O produtor vem do token. O parâmetro `produtorId` existe só para o super
 * admin, que não tem produtor na sessão e precisa dizer sobre qual está
 * trabalhando; para os demais ele é ignorado.
 *
 * <p>A autorização é por método, e não pela classe: quem pode listar não
 * necessariamente pode excluir. O super admin acompanha toda permissão porque
 * não tem papel — ele administra o produto, não um produtor. Esconder o botão
 * no front é conforto; quem barra de verdade é a anotação daqui.
 */
@Path("/usuarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @POST
    @RolesAllowed({Permissao.Codigos.USUARIO_INSERIR, Autorizacao.SUPER_ADMIN})
    public Response criar(@Valid UsuarioRequest request) {
        UsuarioResponse response = usuarioService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @RolesAllowed({Permissao.Codigos.USUARIO_LISTAR, Autorizacao.SUPER_ADMIN})
    public List<UsuarioResponse> listar(@QueryParam("produtorId") UUID produtorId) {
        return usuarioService.listar(produtorId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.USUARIO_LISTAR, Autorizacao.SUPER_ADMIN})
    public UsuarioResponse buscarPorId(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return usuarioService.buscarPorId(id, produtorId);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.USUARIO_EDITAR, Autorizacao.SUPER_ADMIN})
    public UsuarioResponse editar(
            @PathParam("id") UUID id,
            @Valid UsuarioUpdateRequest request,
            @QueryParam("produtorId") UUID produtorId) {
        return usuarioService.editar(id, request, produtorId);
    }

    @PATCH
    @Path("/{id}/bloquear")
    @RolesAllowed({Permissao.Codigos.USUARIO_BLOQUEAR, Autorizacao.SUPER_ADMIN})
    public UsuarioResponse bloquear(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return usuarioService.bloquear(id, produtorId);
    }

    @PATCH
    @Path("/{id}/desbloquear")
    @RolesAllowed({Permissao.Codigos.USUARIO_BLOQUEAR, Autorizacao.SUPER_ADMIN})
    public UsuarioResponse desbloquear(
            @PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        return usuarioService.desbloquear(id, produtorId);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({Permissao.Codigos.USUARIO_EXCLUIR, Autorizacao.SUPER_ADMIN})
    public Response excluir(@PathParam("id") UUID id, @QueryParam("produtorId") UUID produtorId) {
        usuarioService.excluir(id, produtorId);
        return Response.noContent().build();
    }
}
