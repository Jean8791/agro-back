package br.com.codenest.agro.auth.service;

import br.com.codenest.agro.auth.dto.RateLimitRequest;
import br.com.codenest.agro.auth.dto.RateLimitResponse;
import br.com.codenest.agro.auth.entity.ConfiguracaoRateLimit;
import br.com.codenest.agro.auth.repository.ConfiguracaoRateLimitRepository;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Leitura e alteração dos limites do rate limiting de login.
 *
 * <p>Os valores ficam em cache de memória porque são lidos a cada tentativa
 * de login; o cache é trocado a cada alteração pela API. Alteração feita
 * direto no banco (SQL) só passa a valer após reiniciar a aplicação.
 */
@ApplicationScoped
public class ConfiguracaoRateLimitService {

    @Inject
    ConfiguracaoRateLimitRepository configuracaoRateLimitRepository;

    private volatile RateLimitResponse cache;

    public RateLimitResponse limitesAtuais() {
        RateLimitResponse atual = cache;
        if (atual == null) {
            atual = RateLimitResponse.from(buscarEntidade());
            cache = atual;
        }
        return atual;
    }

    @Transactional
    public RateLimitResponse atualizar(RateLimitRequest request) {
        var configuracao = buscarEntidade();
        configuracao.setMaxTentativasEmail(request.maxTentativasEmail());
        configuracao.setMaxTentativasIp(request.maxTentativasIp());
        configuracao.setJanelaMinutos(request.janelaMinutos());
        configuracao.setBloqueioMinutos(request.bloqueioMinutos());

        RateLimitResponse novo = RateLimitResponse.from(configuracao);
        cache = novo;
        return novo;
    }

    private ConfiguracaoRateLimit buscarEntidade() {
        return configuracaoRateLimitRepository.buscarUnica()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Configuração de rate limit não encontrada"));
    }
}
