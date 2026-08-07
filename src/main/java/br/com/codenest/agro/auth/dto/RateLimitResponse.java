package br.com.codenest.agro.auth.dto;

import br.com.codenest.agro.auth.entity.ConfiguracaoRateLimit;

public record RateLimitResponse(
        int maxTentativasEmail,
        int maxTentativasIp,
        int janelaMinutos,
        int bloqueioMinutos) {

    public static RateLimitResponse from(ConfiguracaoRateLimit configuracao) {
        return new RateLimitResponse(
                configuracao.getMaxTentativasEmail(),
                configuracao.getMaxTentativasIp(),
                configuracao.getJanelaMinutos(),
                configuracao.getBloqueioMinutos());
    }
}
