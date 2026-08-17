package br.com.codenest.agro.raiox.produto.service;

import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.raiox.RaioxSemeadorService;
import br.com.codenest.agro.raiox.produto.dto.ProdutoBaseRequest;
import br.com.codenest.agro.raiox.produto.dto.ProdutoBaseResponse;
import br.com.codenest.agro.raiox.produto.entity.RaioxProdutoBase;
import br.com.codenest.agro.raiox.produto.repository.RaioxProdutoBaseRepository;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProdutoBaseService {

    @Inject
    RaioxProdutoBaseRepository produtoBaseRepository;

    @Inject
    EscopoProdutor escopoProdutor;

    @Inject
    RaioxSemeadorService semeadorService;

    public List<ProdutoBaseResponse> listar(UUID produtorInformado) {
        return produtoBaseRepository.listarDoProdutor(escopoProdutor.resolver(produtorInformado)).stream()
                .sorted(Comparator.comparing(RaioxProdutoBase::getProduto))
                .map(ProdutoBaseResponse::from)
                .toList();
    }

    public ProdutoBaseResponse buscarPorId(UUID id, UUID produtorInformado) {
        return ProdutoBaseResponse.from(buscarEntidade(id, escopoProdutor.resolver(produtorInformado)));
    }

    @Transactional
    public ProdutoBaseResponse criar(ProdutoBaseRequest request) {
        UUID produtorId = escopoProdutor.resolver(request.produtorId());

        var entidade = new RaioxProdutoBase();
        entidade.setProdutorId(produtorId);
        preencher(entidade, request);
        produtoBaseRepository.persist(entidade);

        return ProdutoBaseResponse.from(entidade);
    }

    @Transactional
    public ProdutoBaseResponse editar(UUID id, ProdutoBaseRequest request, UUID produtorInformado) {
        var entidade = buscarEntidade(id, escopoProdutor.resolver(produtorInformado));
        preencher(entidade, request);
        return ProdutoBaseResponse.from(entidade);
    }

    @Transactional
    public void excluir(UUID id, UUID produtorInformado) {
        var entidade = buscarEntidade(id, escopoProdutor.resolver(produtorInformado));
        produtoBaseRepository.delete(entidade);
    }

    /** Insere o que falta do catálogo padrão — não duplica o que já existe. */
    @Transactional
    public void semearPadrao(UUID produtorInformado) {
        semeadorService.semear(escopoProdutor.resolver(produtorInformado));
    }

    private void preencher(RaioxProdutoBase entidade, ProdutoBaseRequest request) {
        entidade.setModulo(request.modulo());
        entidade.setCategoria(request.categoria());
        entidade.setProduto(request.produto());
        entidade.setUnidadeTecnica(request.unidadeTecnica());
        entidade.setUnidadeComercial(request.unidadeComercial());
        entidade.setQuantidadePorUnidade(request.quantidadePorUnidade());
        entidade.setUnidadePreco(request.unidadePreco());
        entidade.setTipoProducaoAnimal(request.tipoProducaoAnimal());
        entidade.setNota(request.nota());
        entidade.setFonte(request.fonte());
    }

    private RaioxProdutoBase buscarEntidade(UUID id, UUID produtorId) {
        return produtoBaseRepository
                .buscarDoProdutor(id, produtorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }
}
