package br.com.codenest.agro.shared;

/**
 * Marcadores de autorização que não são permissão de papel.
 *
 * <p>A claim {@code groups} do access token carrega as permissões do papel
 * ({@code usuario:inserir}, …). O super admin é a exceção: ele não pertence a
 * produtor nenhum e não tem papel, então entra em {@code groups} como este
 * marcador único.
 */
public final class Autorizacao {

    /** Quem administra o produto, não um produtor. */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    private Autorizacao() {
    }
}
