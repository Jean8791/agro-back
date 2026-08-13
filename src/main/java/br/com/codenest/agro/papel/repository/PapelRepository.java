package br.com.codenest.agro.papel.repository;

import br.com.codenest.agro.papel.entity.Papel;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PapelRepository implements PanacheRepositoryBase<Papel, UUID> {

    /** Os do produtor mais os de sistema — é a lista que o select de papel oferece. */
    public List<Papel> listarDisponiveis(UUID produtorId) {
        return list("produtorId is null or produtorId = ?1", produtorId);
    }

    /**
     * Busca no escopo de quem pergunta: papel de outro produtor não existe.
     * Papel de sistema é visível a todos.
     */
    public Optional<Papel> buscarDisponivel(UUID id, UUID produtorId) {
        return find("id = ?1 and (produtorId is null or produtorId = ?2)", id, produtorId)
                .firstResultOptional();
    }

    public Optional<Papel> buscarPorNome(String nome, UUID produtorId) {
        return find("nome = ?1 and produtorId = ?2", nome, produtorId).firstResultOptional();
    }
}
