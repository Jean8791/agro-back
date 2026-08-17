package br.com.codenest.agro.raiox;

import br.com.codenest.agro.produtor.ProdutorCriadoEvent;
import br.com.codenest.agro.raiox.checklist.entity.RaioxChecklistItem;
import br.com.codenest.agro.raiox.checklist.repository.RaioxChecklistItemRepository;
import br.com.codenest.agro.raiox.produto.entity.RaioxProdutoBase;
import br.com.codenest.agro.raiox.produto.repository.RaioxProdutoBaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 * Semeia o catálogo padrão ({@link RaioxCatalogoPadrao}) para um produtor.
 *
 * <p>Usado em dois lugares: observando {@link ProdutorCriadoEvent}, pra todo
 * produtor novo já nascer com a base pronta (produtor não importa raiox —
 * é o observador que depende do evento, na direção que evita ciclo entre
 * domínios), e {@code POST /raiox/produtos/semear-padrao}, pra backfillar
 * produtor antigo ou restaurar depois de apagar tudo. Idempotente: só insere
 * o que ainda não existe.
 */
@ApplicationScoped
public class RaioxSemeadorService {

    @Inject
    RaioxProdutoBaseRepository produtoBaseRepository;

    @Inject
    RaioxChecklistItemRepository checklistItemRepository;

    void aoCriarProdutor(@Observes ProdutorCriadoEvent evento) {
        semear(evento.produtorId());
    }

    @Transactional
    public void semear(UUID produtorId) {
        for (var preset : RaioxCatalogoPadrao.PRODUTOS) {
            if (produtoBaseRepository.existePorProdutoECategoria(
                    produtorId, preset.produto(), preset.categoria())) {
                continue;
            }

            var entidade = new RaioxProdutoBase();
            entidade.setProdutorId(produtorId);
            entidade.setModulo(preset.modulo());
            entidade.setCategoria(preset.categoria());
            entidade.setProduto(preset.produto());
            entidade.setUnidadeTecnica(preset.unidadeTecnica());
            entidade.setUnidadeComercial(preset.unidadeComercial());
            entidade.setQuantidadePorUnidade(preset.quantidadePorUnidade());
            entidade.setUnidadePreco(preset.unidadePreco());
            entidade.setTipoProducaoAnimal(preset.tipoProducaoAnimal());
            entidade.setNota(preset.nota());
            entidade.setFonte(preset.fonte());
            produtoBaseRepository.persist(entidade);
        }

        for (var preset : RaioxCatalogoPadrao.CHECKLIST) {
            if (checklistItemRepository.existePorModuloERotulo(produtorId, preset.modulo(), preset.rotulo())) {
                continue;
            }

            var entidade = new RaioxChecklistItem();
            entidade.setProdutorId(produtorId);
            entidade.setModulo(preset.modulo());
            entidade.setRotulo(preset.rotulo());
            entidade.setNota(preset.nota());
            checklistItemRepository.persist(entidade);
        }
    }
}
