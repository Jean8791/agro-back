package br.com.codenest.agro.papel.service;

import br.com.codenest.agro.papel.dto.PapelRequest;
import br.com.codenest.agro.papel.dto.PapelResponse;
import br.com.codenest.agro.papel.entity.Papel;
import br.com.codenest.agro.papel.repository.PapelRepository;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Papéis de um produtor.
 *
 * <p>A listagem devolve os de sistema e os que o produtor criou — é a lista
 * que o formulário de usuário oferece. Alterar e excluir valem só para os
 * próprios: papel de sistema é usado por todos e não pertence a ninguém.
 */
@ApplicationScoped
public class PapelService {

    @Inject
    PapelRepository papelRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

    @Inject
    EscopoProdutor escopoProdutor;

    public List<PapelResponse> listar(UUID produtorInformado) {
        return papelRepository.listarDisponiveis(escopoProdutor.resolver(produtorInformado)).stream()
                // os de sistema primeiro: são a escolha corriqueira
                .sorted(
                        Comparator.comparing(Papel::isSistema)
                                .reversed()
                                .thenComparing(Papel::getNome))
                .map(PapelResponse::from)
                .toList();
    }

    public PapelResponse buscarPorId(UUID id, UUID produtorInformado) {
        return PapelResponse.from(buscarEntidade(id, escopoProdutor.resolver(produtorInformado)));
    }

    @Transactional
    public PapelResponse criar(PapelRequest request) {
        UUID produtorId = escopoProdutor.resolver(request.produtorId());
        exigirNomeLivre(request.nome(), produtorId);

        var papel = new Papel();
        papel.setNome(request.nome());
        papel.setDescricao(request.descricao());
        papel.setProdutorId(produtorId);
        papel.setPermissoes(request.permissoes());
        papelRepository.persist(papel);

        return PapelResponse.from(papel);
    }

    @Transactional
    public PapelResponse editar(UUID id, PapelRequest request, UUID produtorInformado) {
        UUID produtorId = escopoProdutor.resolver(produtorInformado);
        var papel = exigirEditavel(id, produtorId);

        if (!papel.getNome().equals(request.nome())) {
            exigirNomeLivre(request.nome(), produtorId);
        }

        papel.setNome(request.nome());
        papel.setDescricao(request.descricao());
        papel.setPermissoes(request.permissoes());

        return PapelResponse.from(papel);
    }

    @Transactional
    public void excluir(UUID id, UUID produtorInformado) {
        var papel = exigirEditavel(id, escopoProdutor.resolver(produtorInformado));

        long emUso = produtorUsuarioRepository.contarPorPapel(id);
        if (emUso > 0) {
            throw new NegocioException(
                    "Papel em uso por " + emUso + " usuário(s): troque o papel deles antes");
        }

        papelRepository.delete(papel);
    }

    private Papel buscarEntidade(UUID id, UUID produtorId) {
        return papelRepository
                .buscarDisponivel(id, produtorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Papel não encontrado"));
    }

    /**
     * Papel de sistema não se altera, e o de outro produtor nem aparece. Os
     * dois casos respondem a mesma coisa por caminhos diferentes: 400 para o
     * que existe mas é intocável, 404 para o que não é da conta de quem pede.
     */
    private Papel exigirEditavel(UUID id, UUID produtorId) {
        var papel = buscarEntidade(id, produtorId);
        if (!papel.editavelPor(produtorId)) {
            throw new NegocioException("Papel de sistema não pode ser alterado");
        }
        return papel;
    }

    private void exigirNomeLivre(String nome, UUID produtorId) {
        papelRepository
                .buscarPorNome(nome, produtorId)
                .ifPresent(
                        outro -> {
                            throw new NegocioException("Já existe um papel com este nome");
                        });
    }
}
