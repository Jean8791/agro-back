package br.com.codenest.agro.auth;

import br.com.codenest.agro.auth.dto.LoginRequest;
import br.com.codenest.agro.auth.dto.LoginResponse;
import br.com.codenest.agro.auth.dto.RateLimitRequest;
import br.com.codenest.agro.auth.dto.RateLimitResponse;
import br.com.codenest.agro.auth.dto.RefreshRequest;
import br.com.codenest.agro.auth.service.AuthService;
import br.com.codenest.agro.auth.service.ConfiguracaoRateLimitService;
import br.com.codenest.agro.shared.Autorizacao;
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
import java.util.Optional;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Pattern FORMATO_IP = Pattern.compile("[0-9A-Fa-f:.]{2,45}");

    @Inject
    AuthService authService;

    @Inject
    ConfiguracaoRateLimitService configuracaoRateLimitService;

    @Context
    HttpServerRequest httpRequest;

    @ConfigProperty(name = "agro.auth.cabecalho-ip")
    Optional<String> cabecalhoIp;

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
    @RolesAllowed(Autorizacao.SUPER_ADMIN)
    public RateLimitResponse buscarRateLimit() {
        return configuracaoRateLimitService.limitesAtuais();
    }

    @PUT
    @Path("/rate-limit")
    @RolesAllowed(Autorizacao.SUPER_ADMIN)
    public RateLimitResponse atualizarRateLimit(@Valid RateLimitRequest request) {
        return configuracaoRateLimitService.atualizar(request);
    }

    /**
     * IP de origem da requisição, usado no limite de tentativas de login.
     *
     * <p>Por padrão é o endereço da conexão. Atrás de um proxy esse endereço é
     * o do próprio proxy, igual para todo mundo; aí {@code agro.auth.cabecalho-ip}
     * aponta o cabeçalho em que o proxy entrega o IP real (no Render, que fica
     * atrás da Cloudflare, {@code CF-Connecting-IP}). Só vale configurar um
     * cabeçalho que o proxy sempre sobrescreve: {@code X-Forwarded-For} o
     * cliente consegue forjar, e com ele forjaria o próprio IP para escapar do
     * limite.
     */
    private String ipDeOrigem() {
        var doCabecalho = cabecalhoIp
                .filter(nome -> !nome.isBlank())
                .map(httpRequest::getHeader)
                .map(String::strip)
                .filter(valor -> FORMATO_IP.matcher(valor).matches());
        if (doCabecalho.isPresent()) {
            return doCabecalho.get();
        }
        var endereco = httpRequest.remoteAddress();
        return endereco == null ? null : endereco.hostAddress();
    }
}
