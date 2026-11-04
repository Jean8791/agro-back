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

    /** O vínculo da pessoa com o produtor, ativo ou bloqueado. */
    public Optional<ProdutorUsuario> buscar(UUID produtorId, UUID usuarioId) {
        return find("produtorId = ?1 and usuarioId = ?2", produtorId, usuarioId).firstResultOptional();
    }

    /** Se a pessoa tem acesso, ativo ou bloqueado, a algum produtor além deste. */
    public boolean temVinculoForaDe(UUID produtorId, UUID usuarioId) {
        return count("usuarioId = ?1 and produtorId <> ?2", usuarioId, produtorId) > 0;
    }

    /** Quantas pessoas exercem este papel — excluir papel em uso deixaria vínculo órfão. */
    public long contarPorPapel(UUID papelId) {
        return count("papelId", papelId);
    }
}
