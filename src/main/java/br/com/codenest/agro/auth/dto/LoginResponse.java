package br.com.codenest.agro.auth.dto;

public record LoginResponse(String token, long expiresIn, String refreshToken, long refreshExpiresIn) {
}
