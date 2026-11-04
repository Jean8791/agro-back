package br.com.codenest.agro.raiox.simulacao.service;

import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.raiox.simulacao.calculo.CalculadoraPrecoMinimo;
import br.com.codenest.agro.raiox.simulacao.calculo.FormularioCalculo;
import br.com.codenest.agro.raiox.simulacao.calculo.ResultadoCalculo;
import br.com.codenest.agro.raiox.simulacao.dto.SimulacaoRequest;
import br.com.codenest.agro.raiox.simulacao.dto.SimulacaoResponse;
import br.com.codenest.agro.raiox.simulacao.entity.RaioxSimulacao;
import br.com.codenest.agro.raiox.simulacao.repository.RaioxSimulacaoRepository;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

/** Simulação é imutável — só existe criar, listar, buscar e excluir. */
@ApplicationScoped
public class SimulacaoService {

    private static final TypeReference<Map<String, Object>> JSON_OBJETO = new TypeReference<>() {};

    @Inject
    RaioxSimulacaoRepository simulacaoRepository;

    @Inject
    CalculadoraPrecoMinimo calculadora;

    @Inject
    ObjectMapper objectMapper;

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

    public ResultadoCalculo calcular(FormularioCalculo formulario) {
        return calculadora.calcular(formulario);
    }

    @Transactional
    public SimulacaoResponse criar(SimulacaoRequest request) {
        UUID produtorId = escopoProdutor.resolver(request.produtorId());
        FormularioCalculo formulario = request.formulario();
        ResultadoCalculo resultado = calculadora.calcular(formulario);

        if (!resultado.erros().isEmpty()) {
            throw new NegocioException(
                    "Corrija o formulário antes de salvar: " + String.join(" ", resultado.erros()));
        }
        if (formulario.nomeProduto().isEmpty()) {
            throw new NegocioException("Informe o produto ou atividade antes de salvar.");
        }

        var entidade = new RaioxSimulacao();
        entidade.setProdutorId(produtorId);
        entidade.setNome(request.nome().strip());
        entidade.setModulo(formulario.modulo());
        entidade.setProduto(formulario.nomeProduto());
        entidade.setSemaforo(resultado.semaforo());
        entidade.setPrecoMinimo(resultado.precoMinimo().setScale(2, RoundingMode.HALF_UP));
        entidade.setFormulario(objectMapper.convertValue(formulario, JSON_OBJETO));
        entidade.setResultado(objectMapper.convertValue(resultado, JSON_OBJETO));
        simulacaoRepository.persistAndFlush(entidade);

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
