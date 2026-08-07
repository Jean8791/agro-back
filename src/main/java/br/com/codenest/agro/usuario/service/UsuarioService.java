package br.com.codenest.agro.usuario.service;

import br.com.codenest.agro.produtor.entity.ProdutorUsuario;
import br.com.codenest.agro.produtor.enums.Papel;
import br.com.codenest.agro.produtor.repository.ProdutorRepository;
import br.com.codenest.agro.produtor.repository.ProdutorUsuarioRepository;
import br.com.codenest.agro.shared.exception.NegocioException;
import br.com.codenest.agro.shared.exception.RecursoNaoEncontradoException;
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
    ProdutorRepository produtorRepository;

    @Inject
    ProdutorUsuarioRepository produtorUsuarioRepository;

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

        var usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(BcryptUtil.bcryptHash(request.senha()));
        usuarioRepository.persist(usuario);

        var vinculo = new ProdutorUsuario();
        vinculo.setProdutorId(produtorId);
        vinculo.setUsuarioId(usuario.getId());
        vinculo.setPapel(request.papel());
        produtorUsuarioRepository.persist(vinculo);

        return UsuarioResponse.from(usuario, request.papel());
    }

    public List<UsuarioResponse> listar(UUID produtorInformado) {
        return produtorUsuarioRepository.listarDoProdutor(produtorDaOperacao(produtorInformado))
                .stream()
                .map(
                        vinculo ->
                                UsuarioResponse.from(
                                        usuarioRepository.findById(vinculo.getUsuarioId()),
                                        vinculo.getPapel()))
                .toList();
    }

    public UsuarioResponse buscarPorId(UUID id, UUID produtorInformado) {
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        return UsuarioResponse.from(buscarEntidadePorId(id), vinculo.getPapel());
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

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        vinculo.setPapel(request.papel());

        return UsuarioResponse.from(usuario, vinculo.getPapel());
    }

    @Transactional
    public UsuarioResponse bloquear(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        usuario.setAtivo(false);
        usuarioBloqueadoEvent.fire(new UsuarioBloqueadoEvent(id));
        return UsuarioResponse.from(usuario, vinculo.getPapel());
    }

    @Transactional
    public UsuarioResponse desbloquear(UUID id, UUID produtorInformado) {
        var usuario = buscarEntidadePorId(id);
        var vinculo = vinculoObrigatorio(id, produtorInformado);
        usuario.setAtivo(true);
        return UsuarioResponse.from(usuario, vinculo.getPapel());
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
     * Impede que um produtor mexa em usuário de outro: se não há vínculo com o
     * produtor da sessão, o usuário simplesmente não existe para quem pergunta.
     */
    private ProdutorUsuario vinculoObrigatorio(UUID usuarioId, UUID produtorInformado) {
        return produtorUsuarioRepository
                .buscarAtivo(produtorDaOperacao(produtorInformado), usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    /**
     * Produtor sobre o qual a operação age: o da sessão, ou o informado quando
     * quem chama é super admin (que não tem produtor no token).
     */
    private UUID produtorDaOperacao(UUID produtorInformado) {
        if (!tenantContext.isSuperAdmin()) {
            return tenantContext.produtorIdObrigatorio();
        }

        if (produtorInformado == null) {
            throw new NegocioException("Informe o produtor: super admin não tem um na sessão");
        }

        if (produtorRepository.findById(produtorInformado) == null) {
            throw new RecursoNaoEncontradoException("Produtor não encontrado");
        }

        return produtorInformado;
    }
}
