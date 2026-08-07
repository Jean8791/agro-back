package br.com.codenest.agro.auth.entity;

import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Limites da proteção contra força bruta. Configuração global do produto:
 * vale para todos os produtores, e por isso estende {@link BaseEntity}.
 *
 * <p>Tabela de linha única — garantido no banco por um unique sobre a coluna
 * `linha_unica`, que só aceita `true`.
 */
@Entity
@Table(name = "configuracao_rate_limit")
public class ConfiguracaoRateLimit extends BaseEntity {

    @Column(name = "linha_unica", nullable = false)
    private boolean linhaUnica = true;

    @Column(name = "max_tentativas_email", nullable = false)
    private int maxTentativasEmail;

    @Column(name = "max_tentativas_ip", nullable = false)
    private int maxTentativasIp;

    @Column(name = "janela_minutos", nullable = false)
    private int janelaMinutos;

    @Column(name = "bloqueio_minutos", nullable = false)
    private int bloqueioMinutos;

    public boolean isLinhaUnica() {
        return linhaUnica;
    }

    public int getMaxTentativasEmail() {
        return maxTentativasEmail;
    }

    public void setMaxTentativasEmail(int maxTentativasEmail) {
        this.maxTentativasEmail = maxTentativasEmail;
    }

    public int getMaxTentativasIp() {
        return maxTentativasIp;
    }

    public void setMaxTentativasIp(int maxTentativasIp) {
        this.maxTentativasIp = maxTentativasIp;
    }

    public int getJanelaMinutos() {
        return janelaMinutos;
    }

    public void setJanelaMinutos(int janelaMinutos) {
        this.janelaMinutos = janelaMinutos;
    }

    public int getBloqueioMinutos() {
        return bloqueioMinutos;
    }

    public void setBloqueioMinutos(int bloqueioMinutos) {
        this.bloqueioMinutos = bloqueioMinutos;
    }
}
