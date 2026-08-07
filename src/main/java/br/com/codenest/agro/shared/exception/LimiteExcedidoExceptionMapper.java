package br.com.codenest.agro.shared.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;

@Provider
public class LimiteExcedidoExceptionMapper implements ExceptionMapper<LimiteExcedidoException> {

    private static final int TOO_MANY_REQUESTS = 429;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(LimiteExcedidoException exception) {
        var body = new ErrorResponse(
                Instant.now(),
                TOO_MANY_REQUESTS,
                "Too Many Requests",
                exception.getMessage(),
                uriInfo.getPath());

        return Response.status(TOO_MANY_REQUESTS)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
