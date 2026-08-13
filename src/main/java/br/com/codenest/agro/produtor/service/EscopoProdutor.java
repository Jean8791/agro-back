package br.com.codenest.agro.produtor.service;

import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import br.com.codenest.agro.shared.tenant.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Sobre qual produtor a operação age.
 *
 * <p>Para quem tem papel dentro de um produtor, é sempre o do token e o
 * parâmetro informado é ignorado — não adianta pedir para mexer no produtor
 * do vizinho. Só o super admin escolhe, porque só ele não tem produtor na
 * sessão.
 */
@ApplicationScoped
public class EscopoProdutor {

    @Inject
    TenantContext tenantContext;

    @Inject
    ProdutorRepository produtorRepository;

    public UUID resolver(UUID produtorInformado) {
        if (!tenantContext.isSuperAdmin()) {
            return tenantContext.produtorIdObrigatorio();
        }

        if (produtorInformado == null) {
            throw new NegocioException("Informe o produtor: super admin não tem um na sessão");
        }

        if (produtorRepository.findById(produtorInformado) == null) {
            throw new RecursoNaoEncontradoException("Produtor não encontrado");
        }

        return produtorInformado;
    }
}
