package br.com.codenest.agro.papel.entity;

import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Um conjunto de permissões com nome — o que a pessoa faz dentro de um
 * produtor.
 *
 * <p>Era um enum de três valores fixos. Virou tabela para que o ADMIN monte
 * os papéis que a operação dele pede ("Encarregado de campo", "Financeiro")
 * sem depender de quem escreve o código.
 *
 * <p>Estende {@link BaseEntity} e não {@code BaseTenantEntity} de propósito:
 * os papéis de sistema têm {@code produtorId} nulo e valem para todos, e o
 * filtro automático de produtor os esconderia.
 */
@Entity
@Table(
        name = "papel",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_papel_produtor_nome",
                        columnNames = {"produtor_id", "nome"}))
public class Papel extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(length = 200)
    private String descricao;

    /** Nulo nos papéis de sistema, que valem para todo produtor. */
    @Column(name = "produtor_id", updatable = false, columnDefinition = "uuid")
    private UUID produtorId;

    /** Papel que nasce com o sistema: pode ser usado, nunca alterado. */
    @Column(nullable = false, updatable = false)
    private boolean sistema = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "papel_permissao",
            joinColumns = @JoinColumn(name = "papel_id", columnDefinition = "uuid"))
    @Column(name = "permissao", nullable = false, length = 60)
    @Enumerated(EnumType.STRING)
    private Set<Permissao> permissoes = EnumSet.noneOf(Permissao.class);

    /** Se pode ser alterado por quem administra o produtor informado. */
    public boolean editavelPor(UUID produtorDaOperacao) {
        return !sistema && produtorDaOperacao.equals(produtorId);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }

    public boolean isSistema() {
        return sistema;
    }

    public void setSistema(boolean sistema) {
        this.sistema = sistema;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<Permissao> permissoes) {
        this.permissoes =
                permissoes == null || permissoes.isEmpty()
                        ? EnumSet.noneOf(Permissao.class)
                        : EnumSet.copyOf(permissoes);
    }
}
