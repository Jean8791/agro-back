package br.com.codenest.agro.shared.exception;

import jakarta.ws.rs.WebApplicationException;
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
        if (exception instanceof WebApplicationException web && web.getResponse().getStatus() < 500) {
            return respostaDoFramework(web);
        }

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

    private Response respostaDoFramework(WebApplicationException exception) {
        Response original = exception.getResponse();
        int status = original.getStatus();
        String motivo = original.getStatusInfo().getReasonPhrase();

        var body = new ErrorResponse(Instant.now(), status, motivo, mensagem(status, exception), uriInfo.getPath());

        return Response.fromResponse(original)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private static String mensagem(int status, WebApplicationException exception) {
        return switch (status) {
            case 404 -> "Endereço não encontrado.";
            case 405 -> "Método não permitido neste endereço.";
            case 415 -> "Envie o corpo da requisição como application/json.";
            case 400 -> "Requisição inválida: confira o formato do JSON enviado.";
            default -> exception.getMessage();
        };
    }
}
