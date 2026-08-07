package br.com.codenest.agro.shared;

/**
 * Nomes das claims que o access token carrega, além das padrão do MP-JWT
 * (`sub`, `groups`, `exp`).
 */
public final class ClaimsJwt {

    /** E-mail do usuário, para a interface exibir sem precisar consultar. */
    public static final String EMAIL = "email";

    /** Id do produtor (tenant) da sessão. Ausente para o super admin. */
    public static final String PRODUTOR = "produtor";

    private ClaimsJwt() {
    }
}
