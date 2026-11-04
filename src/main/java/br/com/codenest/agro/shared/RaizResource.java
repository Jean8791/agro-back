package br.com.codenest.agro.shared;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

/** A raiz da API leva direto para a documentação interativa. */
@Path("/")
public class RaizResource {

    @GET
    @PermitAll
    @Operation(hidden = true)
    public Response raiz() {
        return Response.status(Response.Status.SEE_OTHER).header("Location", "/swagger-ui").build();
    }
}
