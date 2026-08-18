package br.com.codenest.agro.produtor.service;

import br.com.codenest.agro.produtor.ProdutorCriadoEvent;
import br.com.codenest.agro.produtor.dto.ProdutorRequest;
import br.com.codenest.agro.produtor.dto.ProdutorResponse;
import br.com.codenest.agro.produtor.entity.Produtor;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 * Cadastro dos clientes. Só o super admin chega aqui — é a porta de entrada
 * de um produtor novo no produto.
 */
@ApplicationScoped
public class ProdutorService {

    @Inject
    ProdutorRepository produtorRepository;

    @Inject
    Event<ProdutorCriadoEvent> produtorCriadoEvent;

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

        // quem precisar semear dado inicial (hoje, só o catálogo do Raio-X)
        // observa este evento — produtor não precisa conhecer quem escuta
        produtorCriadoEvent.fire(new ProdutorCriadoEvent(produtor.getId()));

        return ProdutorResponse.from(produtor);
    }

    public PaginaResposta<ProdutorResponse> listar(ParametrosPaginacao paginacao, String termo) {
        var pagina = produtorRepository.paginar(paginacao, termo);
        return PaginaResposta.de(
                pagina.conteudo().stream().map(ProdutorResponse::from).toList(), pagina.total());
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
