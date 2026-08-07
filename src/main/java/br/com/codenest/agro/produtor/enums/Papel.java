package br.com.codenest.agro.produtor.enums;

/**
 * O que a pessoa faz dentro de um produtor. Vai para a claim `groups` do JWT,
 * então também é o que o `@RolesAllowed` enxerga.
 */
public enum Papel {
    /** Administra o produtor: cadastra usuários e configura. */
    ADMIN,
    /** Lança e edita os dados do dia a dia. */
    OPERACIONAL,
    /** Só consulta. */
    CONSULTA;

    /** Papel de quem é usuário do produto, não de um produtor. */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
}
