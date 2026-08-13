package br.com.codenest.agro.papel;

import java.util.UUID;

/**
 * Ids dos papéis que a migration semeia.
 *
 * <p>São fixos porque o {@code V7__papel_permissao.sql} os insere com id
 * literal — foi assim que o vínculo que usava o enum antigo pôde ser
 * convertido. Ter as constantes aqui evita espalhar UUID solto pelo código e
 * pelos testes.
 */
public final class PapeisDeSistema {

    public static final UUID ADMINISTRADOR = UUID.fromString("01890000-0000-7000-8000-000000000010");
    public static final UUID OPERACIONAL = UUID.fromString("01890000-0000-7000-8000-000000000011");
    public static final UUID CONSULTA = UUID.fromString("01890000-0000-7000-8000-000000000012");

    private PapeisDeSistema() {
    }
}
