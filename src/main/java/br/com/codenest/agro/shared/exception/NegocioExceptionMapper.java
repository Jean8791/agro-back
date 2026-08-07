package br.com.codenest.agro.shared.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;

@Provider
public class NegocioExceptionMapper implements ExceptionMapper<NegocioException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NegocioException exception) {
        var body = new ErrorResponse(
                Instant.now(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                exception.getMessage(),
                uriInfo.getPath());

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
