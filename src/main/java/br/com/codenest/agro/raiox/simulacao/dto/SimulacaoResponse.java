package br.com.codenest.agro.raiox.simulacao.dto;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.simulacao.entity.RaioxSimulacao;
import br.com.codenest.agro.raiox.simulacao.enums.Semaforo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record SimulacaoResponse(
        UUID id,
        String nome,
        ModuloProducao modulo,
        String produto,
        Semaforo semaforo,
        BigDecimal precoMinimo,
        Map<String, Object> formulario,
        Map<String, Object> resultado,
        LocalDateTime criadoEm) {

    public static SimulacaoResponse from(RaioxSimulacao entidade) {
        return new SimulacaoResponse(
                entidade.getId(),
                entidade.getNome(),
                entidade.getModulo(),
                entidade.getProduto(),
                entidade.getSemaforo(),
                entidade.getPrecoMinimo(),
                entidade.getFormulario(),
                entidade.getResultado(),
                entidade.getCriadoEm());
    }
}
