package br.com.codenest.agro.usuario.entity;

import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A pessoa que acessa o sistema.
 *
 * <p>Entidade global: o usuário não pertence a um produtor. O vínculo (e o
 * papel que ele exerce em cada um) fica em
 * {@link br.com.codenest.agro.produtor.entity.ProdutorUsuario}.
 */
@Entity
@Table(name = "usuario")
public class Usuario extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Usuário do produto (nós): enxerga todos os produtores, sem filtro. */
    @Column(nullable = false)
    private boolean admin = false;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
