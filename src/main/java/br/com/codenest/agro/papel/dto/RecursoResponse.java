package br.com.codenest.agro.papel.dto;

import br.com.codenest.agro.papel.enums.Acao;
import br.com.codenest.agro.papel.enums.Permissao;
import br.com.codenest.agro.papel.enums.Recurso;
import java.util.Arrays;
import java.util.List;

/**
 * A matriz de permissões como a tela precisa dela: uma linha por recurso, com
 * as ações que aquele recurso admite.
 *
 * <p>Vem do back de propósito — assim uma tela nova aparece na matriz sem
 * ninguém precisar mexer no front.
 */
public record RecursoResponse(String recurso, String rotulo, List<AcaoResponse> acoes) {

    public record AcaoResponse(Permissao permissao, Acao acao, String rotulo) {
    }

    public static List<RecursoResponse> matriz() {
        return Arrays.stream(Recurso.values()).map(RecursoResponse::from).toList();
    }

    private static RecursoResponse from(Recurso recurso) {
        List<AcaoResponse> acoes =
                Permissao.doRecurso(recurso).stream()
                        .map(
                                permissao ->
                                        new AcaoResponse(
                                                permissao,
                                                permissao.getAcao(),
                                                permissao.getAcao().getRotulo()))
                        .toList();

        return new RecursoResponse(recurso.name(), recurso.getRotulo(), acoes);
    }
}
