package br.com.codenest.agro.raiox.simulacao.entity;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.simulacao.enums.Semaforo;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Uma foto de um cálculo da calculadora: o que foi preenchido e o resultado
 * que saiu. É imutável — refazer o cálculo gera uma simulação nova, não edita
 * esta.
 *
 * <p>{@code formulario} e {@code resultado} guardam exatamente o JSON que o
 * front mandou: a fórmula continua rodando só lá (ver CLAUDE.md, seção
 * Raio-X), e o back não tenta reconstruir os tipos de
 * {@code CalculatorForm}/{@code CalculationResult} — só valida que vieram
 * preenchidos. {@code modulo}, {@code produto}, {@code semaforo} e
 * {@code precoMinimo} são cópias soltas dos mesmos dados, gravadas à parte
 * pra listagem pintar a grid sem abrir os dois JSONs.
 */
@Entity
@Table(name = "raiox_simulacao")
public class RaioxSimulacao extends BaseEntity {

    @Column(name = "produtor_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID produtorId;

    @Column(nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ModuloProducao modulo;

    @Column(nullable = false, length = 120)
    private String produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Semaforo semaforo;

    @Column(name = "preco_minimo", precision = 14, scale = 2)
    private BigDecimal precoMinimo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> formulario;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> resultado;

    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public ModuloProducao getModulo() {
        return modulo;
    }

    public void setModulo(ModuloProducao modulo) {
        this.modulo = modulo;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public Semaforo getSemaforo() {
        return semaforo;
    }

    public void setSemaforo(Semaforo semaforo) {
        this.semaforo = semaforo;
    }

    public BigDecimal getPrecoMinimo() {
        return precoMinimo;
    }

    public void setPrecoMinimo(BigDecimal precoMinimo) {
        this.precoMinimo = precoMinimo;
    }

    public Map<String, Object> getFormulario() {
        return formulario;
    }

    public void setFormulario(Map<String, Object> formulario) {
        this.formulario = formulario;
    }

    public Map<String, Object> getResultado() {
        return resultado;
    }

    public void setResultado(Map<String, Object> resultado) {
        this.resultado = resultado;
    }
}
