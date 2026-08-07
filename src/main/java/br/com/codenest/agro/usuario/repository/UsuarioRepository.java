package br.com.codenest.agro.usuario.repository;

import br.com.codenest.agro.usuario.entity.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<Usuario, UUID> {

    public Optional<Usuario> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<Usuario> findByEmailAndIdNot(String email, UUID id) {
        return find("email = ?1 and id != ?2", email, id).firstResultOptional();
    }
}
