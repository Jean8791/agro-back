package br.com.codenest.agro.produtor.repository;

import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProdutorUsuarioRepository implements PanacheRepositoryBase<ProdutorUsuario, UUID> {

    /** Vínculos ativos da pessoa — é o que o login usa para saber onde ela entra. */
    public List<ProdutorUsuario> listarAtivosDoUsuario(UUID usuarioId) {
        return list("usuarioId = ?1 and ativo = true", usuarioId);
    }

    public Optional<ProdutorUsuario> buscarAtivo(UUID produtorId, UUID usuarioId) {
        return find("produtorId = ?1 and usuarioId = ?2 and ativo = true", produtorId, usuarioId)
                .firstResultOptional();
    }

    public List<ProdutorUsuario> listarDoProdutor(UUID produtorId) {
        return list("produtorId", produtorId);
    }
}
