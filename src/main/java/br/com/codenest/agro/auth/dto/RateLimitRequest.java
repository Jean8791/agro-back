package br.com.codenest.agro.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RateLimitRequest(
        @Min(1) @Max(1000) int maxTentativasEmail,
        @Min(1) @Max(10000) int maxTentativasIp,
        @Min(1) @Max(1440) int janelaMinutos,
        @Min(1) @Max(1440) int bloqueioMinutos) {
}
