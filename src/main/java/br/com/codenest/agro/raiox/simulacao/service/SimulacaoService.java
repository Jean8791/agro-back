package br.com.codenest.agro.raiox.simulacao.service;

import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.raiox.simulacao.dto.SimulacaoRequest;
import br.com.codenest.agro.raiox.simulacao.dto.SimulacaoResponse;
import br.com.codenest.agro.raiox.simulacao.entity.RaioxSimulacao;
import br.com.codenest.agro.raiox.simulacao.repository.RaioxSimulacaoRepository;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

/** Simulação é imutável — só existe criar, listar, buscar e excluir. */
@ApplicationScoped
public class SimulacaoService {

    @Inject
    RaioxSimulacaoRepository simulacaoRepository;

    @Inject
    EscopoProdutor escopoProdutor;

    public PaginaResposta<SimulacaoResponse> listar(
            ParametrosPaginacao paginacao, String termo, UUID produtorInformado) {
        var pagina =
                simulacaoRepository.paginar(escopoProdutor.resolver(produtorInformado), paginacao, termo);
        return PaginaResposta.de(
                pagina.conteudo().stream().map(SimulacaoResponse::from).toList(), pagina.total());
    }

    public SimulacaoResponse buscarPorId(UUID id, UUID produtorInformado) {
        return SimulacaoResponse.from(buscarEntidade(id, escopoProdutor.resolver(produtorInformado)));
    }

    @Transactional
    public SimulacaoResponse criar(SimulacaoRequest request) {
        UUID produtorId = escopoProdutor.resolver(request.produtorId());

        var entidade = new RaioxSimulacao();
        entidade.setProdutorId(produtorId);
        entidade.setNome(request.nome());
        entidade.setModulo(request.modulo());
        entidade.setProduto(request.produto());
        entidade.setSemaforo(request.semaforo());
        entidade.setPrecoMinimo(request.precoMinimo());
        entidade.setFormulario(request.formulario());
        entidade.setResultado(request.resultado());
        simulacaoRepository.persist(entidade);

        return SimulacaoResponse.from(entidade);
    }

    @Transactional
    public void excluir(UUID id, UUID produtorInformado) {
        var entidade = buscarEntidade(id, escopoProdutor.resolver(produtorInformado));
        simulacaoRepository.delete(entidade);
    }

    private RaioxSimulacao buscarEntidade(UUID id, UUID produtorId) {
        return simulacaoRepository
                .buscarDoProdutor(id, produtorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Simulação não encontrada"));
    }
}
