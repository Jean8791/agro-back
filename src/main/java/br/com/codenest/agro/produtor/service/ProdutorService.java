package br.com.codenest.agro.produtor.service;

import br.com.codenest.agro.produtor.dto.ProdutorRequest;
import br.com.codenest.agro.produtor.dto.ProdutorResponse;
import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Cadastro dos clientes. Só o super admin chega aqui — é a porta de entrada
 * de um produtor novo no produto.
 */
@ApplicationScoped
public class ProdutorService {

    @Inject
    ProdutorRepository produtorRepository;

    @Transactional
    public ProdutorResponse criar(ProdutorRequest request) {
        produtorRepository
                .findByDocumento(request.documento())
                .ifPresent(
                        produtor -> {
                            throw new NegocioException("Documento já cadastrado");
                        });

        var produtor = new Produtor();
        produtor.setNome(request.nome());
        produtor.setDocumento(request.documento());
        produtor.setTipoPessoa(request.tipoPessoa());
        produtorRepository.persist(produtor);

        return ProdutorResponse.from(produtor);
    }

    public List<ProdutorResponse> listar() {
        return produtorRepository.listAll().stream().map(ProdutorResponse::from).toList();
    }

    public ProdutorResponse buscarPorId(UUID id) {
        return ProdutorResponse.from(buscarEntidadePorId(id));
    }

    @Transactional
    public ProdutorResponse editar(UUID id, ProdutorRequest request) {
        var produtor = buscarEntidadePorId(id);

        produtorRepository
                .findByDocumentoAndIdNot(request.documento(), id)
                .ifPresent(
                        outro -> {
                            throw new NegocioException("Documento já cadastrado");
                        });

        produtor.setNome(request.nome());
        produtor.setDocumento(request.documento());
        produtor.setTipoPessoa(request.tipoPessoa());

        return ProdutorResponse.from(produtor);
    }

    /**
     * Inativar corta o acesso de todo mundo daquele produtor na próxima
     * requisição — o filtro de tenant recusa produtor inativo.
     */
    @Transactional
    public ProdutorResponse inativar(UUID id) {
        var produtor = buscarEntidadePorId(id);
        produtor.setAtivo(false);
        return ProdutorResponse.from(produtor);
    }

    @Transactional
    public ProdutorResponse reativar(UUID id) {
        var produtor = buscarEntidadePorId(id);
        produtor.setAtivo(true);
        return ProdutorResponse.from(produtor);
    }

    private Produtor buscarEntidadePorId(UUID id) {
        var produtor = produtorRepository.findById(id);
        if (produtor == null) {
            throw new RecursoNaoEncontradoException("Produtor não encontrado");
        }
        return produtor;
    }
}
