package br.com.codenest.agro.raiox.checklist.service;

import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.raiox.checklist.dto.ChecklistItemRequest;
import br.com.codenest.agro.raiox.checklist.dto.ChecklistItemResponse;
import br.com.codenest.agro.raiox.checklist.entity.RaioxChecklistItem;
import br.com.codenest.agro.raiox.checklist.repository.RaioxChecklistItemRepository;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
public class ChecklistItemService {

    @Inject
    RaioxChecklistItemRepository checklistItemRepository;

    @Inject
    EscopoProdutor escopoProdutor;

    public PaginaResposta<ChecklistItemResponse> listar(
            ParametrosPaginacao paginacao, String termo, UUID produtorInformado) {
        var pagina =
                checklistItemRepository.paginar(
                        escopoProdutor.resolver(produtorInformado), paginacao, termo);
        return PaginaResposta.de(
                pagina.conteudo().stream().map(ChecklistItemResponse::from).toList(), pagina.total());
    }

    public ChecklistItemResponse buscarPorId(UUID id, UUID produtorInformado) {
        return ChecklistItemResponse.from(buscarEntidade(id, escopoProdutor.resolver(produtorInformado)));
    }

    @Transactional
    public ChecklistItemResponse criar(ChecklistItemRequest request) {
        UUID produtorId = escopoProdutor.resolver(request.produtorId());

        var entidade = new RaioxChecklistItem();
        entidade.setProdutorId(produtorId);
        preencher(entidade, request);
        checklistItemRepository.persist(entidade);

        return ChecklistItemResponse.from(entidade);
    }

    @Transactional
    public ChecklistItemResponse editar(UUID id, ChecklistItemRequest request, UUID produtorInformado) {
        var entidade = buscarEntidade(id, escopoProdutor.resolver(produtorInformado));
        preencher(entidade, request);
        return ChecklistItemResponse.from(entidade);
    }

    @Transactional
    public void excluir(UUID id, UUID produtorInformado) {
        var entidade = buscarEntidade(id, escopoProdutor.resolver(produtorInformado));
        checklistItemRepository.delete(entidade);
    }

    private void preencher(RaioxChecklistItem entidade, ChecklistItemRequest request) {
        entidade.setModulo(request.modulo());
        entidade.setRotulo(request.rotulo());
        entidade.setNota(request.nota());
    }

    private RaioxChecklistItem buscarEntidade(UUID id, UUID produtorId) {
        return checklistItemRepository
                .buscarDoProdutor(id, produtorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de checklist não encontrado"));
    }
}
