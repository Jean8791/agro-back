package br.com.codenest.agro.papel;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import br.com.codenest.agro.shared.JwtTestSupport;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PapelResourceTest {

    @Inject
    JwtTestSupport jwt;

    /** Um produtor próprio por teste: nenhum caso enxerga o dado do outro. */
    private JwtTestSupport.Acesso admin;

    @BeforeEach
    void prepararAcesso() {
        admin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
    }

    private String nomeUnico() {
        return "Papel Teste " + System.nanoTime();
    }

    private String criarPapel(String nome, String... permissoes) {
        String corpoPermissoes =
                java.util.Arrays.stream(permissoes)
                        .map(p -> "\"" + p + "\"")
                        .collect(java.util.stream.Collectors.joining(","));

        return given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "descricao": "Descrição de teste",
                          "permissoes": [%s]
                        }
                        """.formatted(nome, corpoPermissoes))
                .when().post("/papeis")
                .then().statusCode(201)
                .body("nome", equalTo(nome))
                .body("sistema", is(false))
                .extract().path("id");
    }

    @Test
    void criarDeveRetornar201() {
        criarPapel(nomeUnico(), "USUARIO_LISTAR");
    }

    @Test
    void criarComPermissoesVaziasDeveRetornar201() {
        criarPapel(nomeUnico());
    }

    @Test
    void criarComNomeDuplicadoDeveRetornar400() {
        String nome = nomeUnico();
        criarPapel(nome, "USUARIO_LISTAR");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "descricao": "Outro",
                          "permissoes": []
                        }
                        """.formatted(nome))
                .when().post("/papeis")
                .then().statusCode(400)
                .body("message", equalTo("Já existe um papel com este nome"));
    }

    @Test
    void criarSemTokenDeveRetornar401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "descricao": "Sem token",
                          "permissoes": []
                        }
                        """.formatted(nomeUnico()))
                .when().post("/papeis")
                .then().statusCode(401);
    }

    @Test
    void buscarPorIdDeveRetornarPapel() {
        String nome = nomeUnico();
        String id = criarPapel(nome, "USUARIO_LISTAR");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/papeis/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo(nome));
    }

    @Test
    void buscarPapelDeSistemaDeveRetornar200() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/papeis/{id}", PapeisDeSistema.ADMINISTRADOR.toString())
                .then().statusCode(200)
                .body("sistema", is(true));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/papeis/{id}", "00000000-0000-0000-0000-000000000000")
                .then().statusCode(404)
                .body("message", equalTo("Papel não encontrado"));
    }

    @Test
    void editarDeveAtualizarNomeEPermissoes() {
        String id = criarPapel(nomeUnico(), "USUARIO_LISTAR");
        String novoNome = nomeUnico();

        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "descricao": "Editado",
                          "permissoes": ["USUARIO_LISTAR", "USUARIO_EDITAR"]
                        }
                        """.formatted(novoNome))
                .when().put("/papeis/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo(novoNome))
                .body("permissoes.size()", is(2));
    }

    @Test
    void editarPapelDeSistemaDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Consulta",
                          "descricao": "Tentando editar",
                          "permissoes": []
                        }
                        """)
                .when().put("/papeis/{id}", PapeisDeSistema.CONSULTA.toString())
                .then().statusCode(400)
                .body("message", equalTo("Papel de sistema não pode ser alterado"));
    }

    @Test
    void excluirDeveRemoverOPapel() {
        String id = criarPapel(nomeUnico(), "USUARIO_LISTAR");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().delete("/papeis/{id}", id)
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/papeis/{id}", id)
                .then().statusCode(404);
    }

    @Test
    void excluirPapelDeSistemaDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().delete("/papeis/{id}", PapeisDeSistema.OPERACIONAL.toString())
                .then().statusCode(400)
                .body("message", equalTo("Papel de sistema não pode ser alterado"));
    }

    // ---------------------------------------------------------- listagem

    @Test
    void listarDeveTrazerOsTresPapeisDeSistema() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("limit", 10)
                .when().get("/papeis")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.findAll { it.sistema }.size()", is(3));
    }

    @Test
    void listarDeveTrazerOsDeSistemaSempreAntesMesmoPedindoNomeAsc() {
        criarPapel("AAA " + nomeUnico(), "USUARIO_LISTAR");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("sort", "nome,asc")
                .queryParam("limit", 10)
                .when().get("/papeis")
                .then().statusCode(200)
                .body("conteudo[0].sistema", is(true))
                .body("conteudo[1].sistema", is(true))
                .body("conteudo[2].sistema", is(true))
                .body("conteudo[3].sistema", is(false));
    }

    @Test
    void listarDevePaginarEntreOsPapeisDeSistema() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("sort", "nome,asc")
                .queryParam("offset", 0)
                .queryParam("limit", 2)
                .when().get("/papeis")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(2));

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("sort", "nome,asc")
                .queryParam("offset", 2)
                .queryParam("limit", 2)
                .when().get("/papeis")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(1));
    }

    @Test
    void listarComTermoDeveFiltrarPorNomeOuDescricao() {
        String marcador = "Marcador" + System.nanoTime();
        criarPapel(marcador);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("termo", marcador)
                .when().get("/papeis")
                .then().statusCode(200)
                .body("total", is(1))
                .body("conteudo[0].nome", equalTo(marcador));
    }

    @Test
    void listarComTermoSemCorrespondenciaDeveTrazerPaginaVazia() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("termo", "não-existe-" + System.nanoTime())
                .when().get("/papeis")
                .then().statusCode(200)
                .body("conteudo", is(empty()))
                .body("total", is(0));
    }

    @Test
    void listarComSortInvalidoDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("sort", "sistema,asc")
                .when().get("/papeis")
                .then().statusCode(400)
                .body("message", equalTo("Campo de ordenação inválido: sistema"));
    }

    @Test
    void listarNaoDeveTrazerPapelDeOutroProdutor() {
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
        String nomeDoOutro = "Papel Do Outro " + System.nanoTime();

        given()
                .header("Authorization", "Bearer " + outroAdmin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "descricao": "",
                          "permissoes": []
                        }
                        """.formatted(nomeDoOutro))
                .when().post("/papeis")
                .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("termo", nomeDoOutro)
                .when().get("/papeis")
                .then().statusCode(200)
                .body("total", is(0));
    }
}
