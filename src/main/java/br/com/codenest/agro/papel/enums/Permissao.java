package br.com.codenest.agro.papel.enums;

import java.util.Arrays;
import java.util.List;

/**
 * O que se pode fazer no sistema, no formato {@code recurso:acao}.
 *
 * <p>É enum, e não linha de tabela, porque o {@code @RolesAllowed} de cada
 * endpoint precisa citar a permissão como constante de compilação — daí a
 * classe {@link Codigos}. A tabela guarda só o par papel↔permissão.
 *
 * <p>O código é o que vai para a claim {@code groups} do access token, então
 * mudar o texto de um código invalida a autorização de quem já está logado
 * até o próximo refresh. Acrescentar é seguro; renomear, não.
 */
public enum Permissao {
    USUARIO_LISTAR(Codigos.USUARIO_LISTAR, Recurso.USUARIO, Acao.LISTAR),
    USUARIO_INSERIR(Codigos.USUARIO_INSERIR, Recurso.USUARIO, Acao.INSERIR),
    USUARIO_EDITAR(Codigos.USUARIO_EDITAR, Recurso.USUARIO, Acao.EDITAR),
    USUARIO_EXCLUIR(Codigos.USUARIO_EXCLUIR, Recurso.USUARIO, Acao.EXCLUIR),
    USUARIO_BLOQUEAR(Codigos.USUARIO_BLOQUEAR, Recurso.USUARIO, Acao.BLOQUEAR),

    PAPEL_LISTAR(Codigos.PAPEL_LISTAR, Recurso.PAPEL, Acao.LISTAR),
    PAPEL_INSERIR(Codigos.PAPEL_INSERIR, Recurso.PAPEL, Acao.INSERIR),
    PAPEL_EDITAR(Codigos.PAPEL_EDITAR, Recurso.PAPEL, Acao.EDITAR),
    PAPEL_EXCLUIR(Codigos.PAPEL_EXCLUIR, Recurso.PAPEL, Acao.EXCLUIR),

    // recursos separados: catálogo de produtos e checklist de custos podem
    // ser dados a papéis diferentes (ex.: um cuida da base, outro só calcula)
    RAIOXPRODUTO_LISTAR(Codigos.RAIOXPRODUTO_LISTAR, Recurso.RAIOXPRODUTO, Acao.LISTAR),
    RAIOXPRODUTO_INSERIR(Codigos.RAIOXPRODUTO_INSERIR, Recurso.RAIOXPRODUTO, Acao.INSERIR),
    RAIOXPRODUTO_EDITAR(Codigos.RAIOXPRODUTO_EDITAR, Recurso.RAIOXPRODUTO, Acao.EDITAR),
    RAIOXPRODUTO_EXCLUIR(Codigos.RAIOXPRODUTO_EXCLUIR, Recurso.RAIOXPRODUTO, Acao.EXCLUIR),

    RAIOXCHECKLIST_LISTAR(Codigos.RAIOXCHECKLIST_LISTAR, Recurso.RAIOXCHECKLIST, Acao.LISTAR),
    RAIOXCHECKLIST_INSERIR(Codigos.RAIOXCHECKLIST_INSERIR, Recurso.RAIOXCHECKLIST, Acao.INSERIR),
    RAIOXCHECKLIST_EDITAR(Codigos.RAIOXCHECKLIST_EDITAR, Recurso.RAIOXCHECKLIST, Acao.EDITAR),
    RAIOXCHECKLIST_EXCLUIR(Codigos.RAIOXCHECKLIST_EXCLUIR, Recurso.RAIOXCHECKLIST, Acao.EXCLUIR),

    RAIOXSIMULACAO_LISTAR(Codigos.RAIOXSIMULACAO_LISTAR, Recurso.RAIOXSIMULACAO, Acao.LISTAR),
    RAIOXSIMULACAO_INSERIR(Codigos.RAIOXSIMULACAO_INSERIR, Recurso.RAIOXSIMULACAO, Acao.INSERIR),
    RAIOXSIMULACAO_EXCLUIR(Codigos.RAIOXSIMULACAO_EXCLUIR, Recurso.RAIOXSIMULACAO, Acao.EXCLUIR);

    private final String codigo;
    private final Recurso recurso;
    private final Acao acao;

    Permissao(String codigo, Recurso recurso, Acao acao) {
        this.codigo = codigo;
        this.recurso = recurso;
        this.acao = acao;
    }

    public String getCodigo() {
        return codigo;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public Acao getAcao() {
        return acao;
    }

    /** As permissões de um recurso, na ordem em que a matriz da tela as mostra. */
    public static List<Permissao> doRecurso(Recurso recurso) {
        return Arrays.stream(values()).filter(permissao -> permissao.recurso == recurso).toList();
    }

    /**
     * Os códigos como o {@code @RolesAllowed} precisa vê-los: constantes de
     * compilação. Ficam aqui, coladas no enum, para não haver duas listas.
     */
    public static final class Codigos {

        public static final String USUARIO_LISTAR = "usuario:listar";
        public static final String USUARIO_INSERIR = "usuario:inserir";
        public static final String USUARIO_EDITAR = "usuario:editar";
        public static final String USUARIO_EXCLUIR = "usuario:excluir";
        public static final String USUARIO_BLOQUEAR = "usuario:bloquear";

        public static final String PAPEL_LISTAR = "papel:listar";
        public static final String PAPEL_INSERIR = "papel:inserir";
        public static final String PAPEL_EDITAR = "papel:editar";
        public static final String PAPEL_EXCLUIR = "papel:excluir";

        public static final String RAIOXPRODUTO_LISTAR = "raioxproduto:listar";
        public static final String RAIOXPRODUTO_INSERIR = "raioxproduto:inserir";
        public static final String RAIOXPRODUTO_EDITAR = "raioxproduto:editar";
        public static final String RAIOXPRODUTO_EXCLUIR = "raioxproduto:excluir";

        public static final String RAIOXCHECKLIST_LISTAR = "raioxchecklist:listar";
        public static final String RAIOXCHECKLIST_INSERIR = "raioxchecklist:inserir";
        public static final String RAIOXCHECKLIST_EDITAR = "raioxchecklist:editar";
        public static final String RAIOXCHECKLIST_EXCLUIR = "raioxchecklist:excluir";

        public static final String RAIOXSIMULACAO_LISTAR = "raioxsimulacao:listar";
        public static final String RAIOXSIMULACAO_INSERIR = "raioxsimulacao:inserir";
        public static final String RAIOXSIMULACAO_EXCLUIR = "raioxsimulacao:excluir";

        private Codigos() {
        }
    }
}
