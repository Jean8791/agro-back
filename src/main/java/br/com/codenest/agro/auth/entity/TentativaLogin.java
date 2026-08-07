package br.com.codenest.agro.auth.entity;

import br.com.codenest.agro.auth.enums.TipoTentativa;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Uma falha de login. O momento da tentativa é o {@code criadoEm} herdado de
 * {@link BaseEntity} — não há campo de data próprio.
 */
@Entity
@Table(name = "tentativa_login")
public class TentativaLogin extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTentativa tipo;

    @Column(nullable = false)
    private String chave;

    public TipoTentativa getTipo() {
        return tipo;
    }

    public void setTipo(TipoTentativa tipo) {
        this.tipo = tipo;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}
