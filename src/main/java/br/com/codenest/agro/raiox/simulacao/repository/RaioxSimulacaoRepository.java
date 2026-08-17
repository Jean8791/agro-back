package br.com.codenest.agro.raiox.simulacao.repository;

import br.com.codenest.agro.raiox.simulacao.entity.RaioxSimulacao;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RaioxSimulacaoRepository implements PanacheRepositoryBase<RaioxSimulacao, UUID> {

    /** Mais recente primeiro: é o que se quer ver primeiro no histórico. */
    public List<RaioxSimulacao> listarDoProdutor(UUID produtorId) {
        return list("produtorId = ?1 order by criadoEm desc", produtorId);
    }

    public Optional<RaioxSimulacao> buscarDoProdutor(UUID id, UUID produtorId) {
        return find("id = ?1 and produtorId = ?2", id, produtorId).firstResultOptional();
    }
}
