package br.com.codenest.agro.auth;

import br.com.codenest.agro.auth.dto.LoginRequest;
import br.com.codenest.agro.auth.dto.LoginResponse;
import br.com.codenest.agro.auth.dto.RateLimitRequest;
import br.com.codenest.agro.auth.dto.RateLimitResponse;
import br.com.codenest.agro.auth.dto.RefreshRequest;
import br.com.codenest.agro.auth.service.AuthService;
import br.com.codenest.agro.auth.service.ConfiguracaoRateLimitService;
import br.com.codenest.agro.produtor.enums.Papel;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    ConfiguracaoRateLimitService configuracaoRateLimitService;

    @Context
    HttpServerRequest httpRequest;

    @POST
    @Path("/login")
    @PermitAll
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.autenticar(request, ipDeOrigem());
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public LoginResponse refresh(@Valid RefreshRequest request) {
        return authService.renovar(request);
    }

    @POST
    @Path("/logout")
    @PermitAll
    public Response logout(@Valid RefreshRequest request) {
        authService.logout(request);
        return Response.noContent().build();
    }

    @GET
    @Path("/rate-limit")
    @RolesAllowed(Papel.SUPER_ADMIN)
    public RateLimitResponse buscarRateLimit() {
        return configuracaoRateLimitService.limitesAtuais();
    }

    @PUT
    @Path("/rate-limit")
    @RolesAllowed(Papel.SUPER_ADMIN)
    public RateLimitResponse atualizarRateLimit(@Valid RateLimitRequest request) {
        return configuracaoRateLimitService.atualizar(request);
    }

    /**
     * IP de origem da requisição. Cabeçalhos de proxy (X-Forwarded-For) só são
     * considerados quando {@code quarkus.http.proxy.proxy-address-forwarding}
     * estiver habilitado — sem isso o valor viria do cliente e qualquer um
     * poderia forjar o IP para escapar do limite por origem.
     */
    private String ipDeOrigem() {
        var endereco = httpRequest.remoteAddress();
        return endereco == null ? null : endereco.hostAddress();
    }
}
