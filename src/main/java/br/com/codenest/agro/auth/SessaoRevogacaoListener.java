package br.com.codenest.agro.auth;

import br.com.codenest.agro.auth.repository.SessaoRepository;
import br.com.codenest.agro.usuario.UsuarioBloqueadoEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SessaoRevogacaoListener {

    @Inject
    SessaoRepository sessaoRepository;

    @Transactional
    public void aoBloquear(@Observes UsuarioBloqueadoEvent evento) {
        sessaoRepository.revogarTodasDoUsuario(evento.usuarioId());
    }
}
