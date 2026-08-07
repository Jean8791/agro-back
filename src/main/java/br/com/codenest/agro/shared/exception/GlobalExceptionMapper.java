package br.com.codenest.agro.shared.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Erro não tratado", exception);

        var body = new ErrorResponse(
                Instant.now(),
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                uriInfo.getPath());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
