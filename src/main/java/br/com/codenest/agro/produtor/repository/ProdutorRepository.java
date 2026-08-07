package br.com.codenest.agro.produtor.repository;

import br.com.codenest.agro.produtor.entity.Produtor;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProdutorRepository implements PanacheRepositoryBase<Produtor, UUID> {

    public Optional<Produtor> findByDocumento(String documento) {
        return find("documento", documento).firstResultOptional();
    }

    public Optional<Produtor> findByDocumentoAndIdNot(String documento, UUID id) {
        return find("documento = ?1 and id != ?2", documento, id).firstResultOptional();
    }
}
