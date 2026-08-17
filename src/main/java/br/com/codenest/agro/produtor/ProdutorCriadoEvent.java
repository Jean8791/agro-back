package br.com.codenest.agro.produtor;

import java.util.UUID;

/**
 * Disparado por {@code ProdutorService.criar()} depois do persist.
 *
 * <p>Existe pra quem precisa semear dado inicial de um produtor novo (hoje,
 * só o catálogo do Raio-X) não precisar ser importado por {@code produtor} —
 * a dependência fica só na direção contrária (o observador depende de
 * produtor, produtor não depende de ninguém), como a regra de domínios sem
 * ciclo pede.
 */
public record ProdutorCriadoEvent(UUID produtorId) {
}
