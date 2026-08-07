package br.com.codenest.agro.shared.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;

@Provider
public class RecursoNaoEncontradoExceptionMapper implements ExceptionMapper<RecursoNaoEncontradoException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(RecursoNaoEncontradoException exception) {
        var body = new ErrorResponse(
                Instant.now(),
                Response.Status.NOT_FOUND.getStatusCode(),
                "Not Found",
                exception.getMessage(),
                uriInfo.getPath());

        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
