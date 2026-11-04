package br.com.codenest.agro.usuario.service;

import br.com.codenest.agro.papel.entity.Papel;
import br.com.codenest.agro.papel.repository.PapelRepository;
import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
import br.com.codenest.agro.shared.paginacao.PaginaResposta;
import br.com.codenest.agro.shared.paginacao.ParametrosPaginacao;
import br.com.codenest.agro.shared.tenant.TenantContext;
import br.com.codenest.agro.usuario.UsuarioBloqueadoEvent;
import br.com.codenest.agro.usuario.dto.UsuarioRequest;
import br.com.codenest.agro.usuario.dto.UsuarioResponse;
import br.com.codenest.agro.usuario.dto.UsuarioUpdateRequest;
import br.com.codenest.agro.usuario.entity.Usuario;
import br.com.codenest.agro.usuario.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 * Usuários de um produtor.
 *
 * <p>A listagem e a busca são sempre no escopo do produtor da sessão — o
 * vínculo {@link ProdutorUsuario} é que define quem aparece. Super admin
 * informa o produtor explicitamente.
 */
@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

    @Inject
    PapelRepository papelRepository;

    @Inject
    EscopoProdutor escopoProdutor;

    @Inject
    TenantContext tenantContext;

    @Inject
    Event<UsuarioBloqueadoEvent> usuarioBloqueadoEvent;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        usuarioRepository
                .findByEmail(request.email())
                .ifPresent(
                        usuario -> {
                            throw new NegocioException("E-mail já cadastrado");
                        });

        UUID produtorId = produtorDaOperacao(request.produtorId());
        var papel = papelObrigatorio(request.papelId(), produtorId);

        var usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(BcryptUtil.bcryptHash(request.senha()));
        usuarioRepository.persist(usuario);

        var vinculo = new ProdutorUsuario();
        vinculo.setProdutorId(produtorId);
        vinculo.setUsuarioId(usuario.getId());
        vinculo.setPapelId(papel.getId());
        produtorUsuarioRepository.persist(vinculo);

        return UsuarioResponse.from(usuario, vinculo, papel);
    }

    public PaginaResposta<UsuarioResponse> listar(
            ParametrosPaginacao paginacao, String termo, UUID produtorInformado) {
        var pagina =
                usuarioRepository.paginarDoProdutor(
                        produtorDaOperacao(produtorInformado), paginacao, termo);
        var conteudo =
                pagina.conteudo().stream()
                        .map(linha -> UsuarioResponse.from(
                                (Usuario) linha[0], (ProdutorUsuario) linha[1], (Papel) linha[2]))
                        .toList();
        return PaginaResposta.de(conteudo, pagina.total());
    }

    public UsuarioResponse buscarPorId(UUID id, UUID produtorInformado) {
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        return resposta(buscarEntidadePorId(id), vinculo);
    }

    /**
     * Papel é do vínculo, então muda sempre. Nome e e-mail são da conta, que
     * pode ter acesso a outros produtores: aí só o super admin altera, para um
     * produtor não mexer no login que outro também usa.
     */
    @Transactional
    public UsuarioResponse editar(UUID id, UsuarioUpdateRequest request, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        var papel = papelObrigatorio(request.papelId(), vinculo.getProdutorId());

        boolean mudaConta = !usuario.getNome().equals(request.nome())
                || !usuario.getEmail().equalsIgnoreCase(request.email());
        if (mudaConta) {
            if (!tenantContext.isSuperAdmin()
                    && produtorUsuarioRepository.temVinculoForaDe(vinculo.getProdutorId(), id)) {
                throw new NegocioException(
                        "Este usuário também acessa outro produtor. Nome e e-mail só podem ser alterados pelo suporte.");
            }
            usuarioRepository
                    .findByEmailAndIdNot(request.email(), id)
                    .ifPresent(
                            outro -> {
                                throw new NegocioException("E-mail já cadastrado");
                            });
            usuario.setNome(request.nome());
            usuario.setEmail(request.email());
        }

        vinculo.setPapelId(papel.getId());
        return UsuarioResponse.from(usuario, vinculo, papel);
    }

    /** Bloqueia o acesso a este produtor; os outros produtores da pessoa não são afetados. */
    @Transactional
    public UsuarioResponse bloquear(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        vinculo.setAtivo(false);
        usuarioBloqueadoEvent.fire(new UsuarioBloqueadoEvent(id, vinculo.getProdutorId()));
        return resposta(usuario, vinculo);
    }

    /**
     * Libera o acesso a este produtor. Se a conta inteira estiver inativa e a
     * pessoa só tiver este produtor, a conta é reativada junto.
     */
    @Transactional
    public UsuarioResponse desbloquear(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        vinculo.setAtivo(true);
        if (!usuario.isAtivo() && !produtorUsuarioRepository.temVinculoForaDe(vinculo.getProdutorId(), id)) {
            usuario.setAtivo(true);
        }
        return resposta(usuario, vinculo);
    }

    /**
     * Tira a pessoa deste produtor. A conta só é inativada quando não sobra
     * acesso a nenhum outro.
     */
    @Transactional
    public void excluir(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        UUID produtorId = vinculo.getProdutorId();
        produtorUsuarioRepository.delete(vinculo);
        if (!produtorUsuarioRepository.temVinculoForaDe(produtorId, id)) {
            usuario.setAtivo(false);
        }
        usuarioBloqueadoEvent.fire(new UsuarioBloqueadoEvent(id, produtorId));
    }

    private UsuarioResponse resposta(Usuario usuario, ProdutorUsuario vinculo) {
        return UsuarioResponse.from(usuario, vinculo, papelRepository.findById(vinculo.getPapelId()));
    }

    private Usuario buscarEntidadePorId(UUID id) {
        var usuario = usuarioRepository.findById(id);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }
        return usuario;
    }

    /**
     * O papel escolhido, desde que exista para este produtor: um de sistema ou
     * um que ele mesmo criou. Papel de outro produtor não existe para quem
     * pergunta — não dá para atribuir por id direto.
     */
    private Papel papelObrigatorio(UUID papelId, UUID produtorId) {
        return papelRepository
                .buscarDisponivel(papelId, produtorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Papel não encontrado"));
    }

    /**
     * Impede que um produtor mexa em usuário de outro: se não há vínculo com o
     * produtor da sessão, o usuário simplesmente não existe para quem pergunta.
     */
    private ProdutorUsuario vinculoObrigatorio(UUID usuarioId, UUID produtorInformado) {
        return produtorUsuarioRepository
                .buscar(produtorDaOperacao(produtorInformado), usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private UUID produtorDaOperacao(UUID produtorInformado) {
        return escopoProdutor.resolver(produtorInformado);
    }
}
