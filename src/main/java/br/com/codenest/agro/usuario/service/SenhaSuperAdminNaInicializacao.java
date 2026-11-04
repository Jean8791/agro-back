package br.com.codenest.agro.usuario.service;

import br.com.codenest.agro.usuario.entity.Usuario;
import br.com.codenest.agro.usuario.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Aplica ao super admin de bootstrap a senha definida em
 * {@code agro.super-admin.senha} (variável {@code AGRO_SUPER_ADMIN_SENHA}).
 *
 * <p>A migration {@code V6} cria o super admin com uma senha conhecida, que
 * está no repositório. Em qualquer ambiente publicado essa variável deve
 * estar definida: a cada inicialização a senha guardada é conferida e, se
 * for diferente, substituída.
 */
@ApplicationScoped
public class SenhaSuperAdminNaInicializacao {

    private static final Logger LOG = Logger.getLogger(SenhaSuperAdminNaInicializacao.class);
    private static final int TAMANHO_MINIMO = 12;

    @Inject
    UsuarioRepository usuarioRepository;

    @ConfigProperty(name = "agro.super-admin.email", defaultValue = "admin@agro.com")
    String email;

    @ConfigProperty(name = "agro.super-admin.senha")
    Optional<String> senha;

    @Transactional
    @ActivateRequestContext
    void aoIniciar(@Observes StartupEvent evento) {
        var nova = senha.map(String::strip).filter(valor -> !valor.isEmpty()).orElse(null);
        if (nova == null) {
            return;
        }
        if (nova.length() < TAMANHO_MINIMO) {
            throw new IllegalStateException(
                    "agro.super-admin.senha precisa ter pelo menos " + TAMANHO_MINIMO + " caracteres");
        }

        Usuario admin = usuarioRepository.findByEmail(email).filter(Usuario::isAdmin).orElse(null);
        if (admin == null) {
            LOG.warnf("Super admin %s não encontrado; senha da configuração não aplicada", email);
            return;
        }

        if (!BcryptUtil.matches(nova, admin.getSenha())) {
            admin.setSenha(BcryptUtil.bcryptHash(nova));
            LOG.infof("Senha do super admin %s atualizada a partir da configuração", email);
        }
    }
}
