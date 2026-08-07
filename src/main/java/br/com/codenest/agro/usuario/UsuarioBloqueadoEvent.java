package br.com.codenest.agro.usuario;

import java.util.UUID;

/** Disparado quando o acesso de um usuário é cortado (bloqueio ou exclusão). */
public record UsuarioBloqueadoEvent(UUID usuarioId) {
}
