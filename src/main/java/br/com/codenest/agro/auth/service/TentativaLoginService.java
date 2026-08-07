package br.com.codenest.agro.auth.service;

import br.com.codenest.agro.auth.dto.RateLimitResponse;
import br.com.codenest.agro.auth.entity.TentativaLogin;
import br.com.codenest.agro.auth.enums.TipoTentativa;
import br.com.codenest.agro.auth.repository.TentativaLoginRepository;
import br.com.codenest.agro.shared.exception.LimiteExcedidoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Proteção contra força bruta no login.
 *
 * <p>Duas contagens independentes: por e-mail (protege a conta) e por IP
 * (barra quem varre vários e-mails da mesma origem). A regra é a mesma para
 * as duas: atingido o limite de falhas dentro da janela, o login fica barrado
 * por {@code bloqueioMinutos} contados a partir da última falha. O bloqueio
 * expira sozinho e um login bem-sucedido zera o histórico — o campo
 * {@code ativo} do usuário nunca é tocado aqui.
 *
 * <p>Os limites vêm de {@link ConfiguracaoRateLimitService}.
 */
@ApplicationScoped
public class TentativaLoginService {

    @Inject
    TentativaLoginRepository tentativaLoginRepository;

    @Inject
    ConfiguracaoRateLimitService configuracaoRateLimitService;

    /**
     * Barra a requisição com 429 se o e-mail ou o IP estiverem bloqueados.
     */
    public void verificarBloqueio(String email, String ip) {
        RateLimitResponse limites = configuracaoRateLimitService.limitesAtuais();
        verificarChave(TipoTentativa.EMAIL, email, limites.maxTentativasEmail(), limites);
        verificarChave(TipoTentativa.IP, ip, limites.maxTentativasIp(), limites);
    }

    /**
     * Registra a falha nas duas contagens. Roda em transação própria porque a
     * chamadora aborta com exceção logo em seguida — no fluxo da transação do
     * login, o rollback apagaria o registro.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrarFalha(String email, String ip) {
        RateLimitResponse limites = configuracaoRateLimitService.limitesAtuais();
        LocalDateTime limiteExpurgo = LocalDateTime.now()
                .minusMinutes(limites.janelaMinutos() + (long) limites.bloqueioMinutos());

        registrarChave(TipoTentativa.EMAIL, email, limiteExpurgo);
        registrarChave(TipoTentativa.IP, ip, limiteExpurgo);
    }

    /**
     * Zera o histórico das duas contagens após um login bem-sucedido.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void limparHistorico(String email, String ip) {
        if (preenchida(email)) {
            tentativaLoginRepository.apagarDaChave(TipoTentativa.EMAIL, email);
        }
        if (preenchida(ip)) {
            tentativaLoginRepository.apagarDaChave(TipoTentativa.IP, ip);
        }
    }

    private void verificarChave(TipoTentativa tipo, String chave, int limite, RateLimitResponse limites) {
        if (!preenchida(chave)) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioJanela = agora.minusMinutes(limites.janelaMinutos());

        if (tentativaLoginRepository.contarDesde(tipo, chave, inicioJanela) < limite) {
            return;
        }

        LocalDateTime liberaEm = tentativaLoginRepository.ultimaDesde(tipo, chave, inicioJanela)
                .orElse(agora)
                .plusMinutes(limites.bloqueioMinutos());

        if (agora.isBefore(liberaEm)) {
            throw new LimiteExcedidoException(
                    "Muitas tentativas de login. Tente novamente em %d minuto(s)."
                            .formatted(minutosArredondadosParaCima(agora, liberaEm)));
        }
    }

    private void registrarChave(TipoTentativa tipo, String chave, LocalDateTime limiteExpurgo) {
        if (!preenchida(chave)) {
            return;
        }

        tentativaLoginRepository.apagarAnterioresA(tipo, chave, limiteExpurgo);

        var tentativa = new TentativaLogin();
        tentativa.setTipo(tipo);
        tentativa.setChave(chave);
        tentativaLoginRepository.persist(tentativa);
    }

    private static long minutosArredondadosParaCima(LocalDateTime de, LocalDateTime ate) {
        long segundos = Duration.between(de, ate).getSeconds();
        return Math.max(1, (segundos + 59) / 60);
    }

    private static boolean preenchida(String chave) {
        return chave != null && !chave.isBlank();
    }
}
