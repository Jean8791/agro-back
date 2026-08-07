package br.com.codenest.agro.shared.exception;

/**
 * Operação recusada por exceder um limite de uso (429 Too Many Requests).
 */
public class LimiteExcedidoException extends RuntimeException {

    public LimiteExcedidoException(String message) {
        super(message);
    }
}
