package br.com.codenest.agro.produtor.entity;

import br.com.codenest.agro.produtor.enums.TipoPessoa;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * O cliente que assina o produto. Um produtor tem N propriedades (as fazendas
 * dele) — não confundir os dois.
 *
 * <p>Entidade global: estende {@link BaseEntity}, não a versão com tenant,
 * porque é justamente ela que define o tenant.
 */
@Entity
@Table(name = "produtor")
public class Produtor extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    /** CPF ou CNPJ, só dígitos. */
    @Column(nullable = false, unique = true, length = 14)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 2)
    private TipoPessoa tipoPessoa;

    @Column(nullable = false)
    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
