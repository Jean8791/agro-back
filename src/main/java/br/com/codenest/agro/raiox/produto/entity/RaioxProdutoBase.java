package br.com.codenest.agro.raiox.produto.entity;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import br.com.codenest.agro.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Um produto ou atividade do catálogo do Raio-X, com as unidades que a
 * calculadora usa pra converter produção técnica em unidade comercial de
 * venda.
 *
 * <p>Estende {@link BaseEntity}, não {@code BaseTenantEntity}: o
 * {@code produtor_id} é gravado à mão (via {@code EscopoProdutor}, mesmo
 * caminho de {@code Papel}), porque aqui não existe filtro automático de
 * sessão para o caso do super admin escolher o produtor por query param.
 */
@Entity
@Table(name = "raiox_produto_base")
public class RaioxProdutoBase extends BaseEntity {

    @Column(name = "produtor_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID produtorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ModuloProducao modulo;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false, length = 120)
    private String produto;

    @Column(name = "unidade_tecnica", nullable = false, length = 40)
    private String unidadeTecnica;

    @Column(name = "unidade_comercial", nullable = false, length = 40)
    private String unidadeComercial;

    @Column(name = "quantidade_por_unidade", nullable = false)
    private double quantidadePorUnidade;

    @Column(name = "unidade_preco", nullable = false, length = 30)
    private String unidadePreco;

    /** Só se aplica ao módulo animal; nulo em produtos vegetais. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producao_animal", length = 12)
    private TipoProducaoAnimal tipoProducaoAnimal;

    @Column(length = 400)
    private String nota;

    @Column(length = 300)
    private String fonte;

    public UUID getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(UUID produtorId) {
        this.produtorId = produtorId;
    }

    public ModuloProducao getModulo() {
        return modulo;
    }

    public void setModulo(ModuloProducao modulo) {
        this.modulo = modulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public String getUnidadeTecnica() {
        return unidadeTecnica;
    }

    public void setUnidadeTecnica(String unidadeTecnica) {
        this.unidadeTecnica = unidadeTecnica;
    }

    public String getUnidadeComercial() {
        return unidadeComercial;
    }

    public void setUnidadeComercial(String unidadeComercial) {
        this.unidadeComercial = unidadeComercial;
    }

    public double getQuantidadePorUnidade() {
        return quantidadePorUnidade;
    }

    public void setQuantidadePorUnidade(double quantidadePorUnidade) {
        this.quantidadePorUnidade = quantidadePorUnidade;
    }

    public String getUnidadePreco() {
        return unidadePreco;
    }

    public void setUnidadePreco(String unidadePreco) {
        this.unidadePreco = unidadePreco;
    }

    public TipoProducaoAnimal getTipoProducaoAnimal() {
        return tipoProducaoAnimal;
    }

    public void setTipoProducaoAnimal(TipoProducaoAnimal tipoProducaoAnimal) {
        this.tipoProducaoAnimal = tipoProducaoAnimal;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }
}
