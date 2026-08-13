package br.com.codenest.agro.usuario.service;

import br.com.codenest.agro.papel.entity.Papel;
import br.com.codenest.agro.papel.repository.PapelRepository;
import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.produtor.service.EscopoProdutor;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
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
import java.util.List;
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

        return UsuarioResponse.from(usuario, papel);
    }

    public List<UsuarioResponse> listar(UUID produtorInformado) {
        return produtorUsuarioRepository.listarDoProdutor(produtorDaOperacao(produtorInformado))
                .stream()
                .map(
                        vinculo ->
                                UsuarioResponse.from(
                                        usuarioRepository.findById(vinculo.getUsuarioId()),
                                        papelRepository.findById(vinculo.getPapelId())))
                .toList();
    }

    public UsuarioResponse buscarPorId(UUID id, UUID produtorInformado) {
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        return UsuarioResponse.from(
                buscarEntidadePorId(id), papelRepository.findById(vinculo.getPapelId()));
    }

    @Transactional
    public UsuarioResponse editar(UUID id, UsuarioUpdateRequest request, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);

        usuarioRepository
                .findByEmailAndIdNot(request.email(), id)
                .ifPresent(
                        outro -> {
                            throw new NegocioException("E-mail já cadastrado");
                        });

        var papel = papelObrigatorio(request.papelId(), vinculo.getProdutorId());

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        vinculo.setPapelId(papel.getId());

        return UsuarioResponse.from(usuario, papel);
    }

    @Transactional
    public UsuarioResponse bloquear(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        usuario.setAtivo(false);
        usuarioBloqueadoEvent.fire(new UsuarioBloqueadoEvent(id));
        return UsuarioResponse.from(usuario, papelRepository.findById(vinculo.getPapelId()));
    }

    @Transactional
    public UsuarioResponse desbloquear(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        usuario.setAtivo(true);
        return UsuarioResponse.from(usuario, papelRepository.findById(vinculo.getPapelId()));
    }

    @Transactional
    public void excluir(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        vinculoObrigatorio(id, produtorInformado);
        usuario.setAtivo(false);
        usuarioBloqueadoEvent.fire(new UsuarioBloqueadoEvent(id));
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
                .buscarAtivo(produtorDaOperacao(produtorInformado), usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private UUID produtorDaOperacao(UUID produtorInformado) {
        return escopoProdutor.resolver(produtorInformado);
    }
}
