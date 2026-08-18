package br.com.codenest.agro.raiox.simulacao;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import br.com.codenest.agro.papel.PapeisDeSistema;
import br.com.codenest.agro.shared.JwtTestSupport;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SimulacaoResourceTest {

    @Inject
    JwtTestSupport jwt;

    /** Um produtor próprio por teste: nenhum caso enxerga o dado do outro. */
    private JwtTestSupport.Acesso admin;

    @BeforeEach
    void prepararAcesso() {
        admin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
    }

    private String nomeUnico() {
        return "Simulação Teste " + System.nanoTime();
    }

    private String criarSimulacao(String nome, String produto) {
        return given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "modulo": "VEGETAL",
                          "produto": "%s",
                          "semaforo": "GREEN",
                          "precoMinimo": 12.50,
                          "formulario": { "produtoSelecionado": "%s" },
                          "resultado": { "mensagem": "Preço acima do mínimo." },
                          "produtorId": "%s"
                        }
                        """.formatted(nome, produto, produto, admin.produtorId()))
                .when().post("/raiox/simulacoes")
                .then().statusCode(201)
                .body("nome", equalTo(nome))
                .body("produto", equalTo(produto))
                .body("semaforo", equalTo("GREEN"))
                .extract().path("id");
    }

    @Test
    void criarDeveRetornar201() {
        criarSimulacao(nomeUnico(), "Alface");
    }

    @Test
    void criarComFormularioVazioDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "modulo": "VEGETAL",
                          "produto": "Alface",
                          "semaforo": "GREEN",
                          "formulario": {},
                          "resultado": { "mensagem": "x" },
                          "produtorId": "%s"
                        }
                        """.formatted(nomeUnico(), admin.produtorId()))
                .when().post("/raiox/simulacoes")
                .then().statusCode(400);
    }

    @Test
    void criarSemTokenDeveRetornar401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "modulo": "VEGETAL",
                          "produto": "Alface",
                          "semaforo": "GREEN",
                          "formulario": { "a": 1 },
                          "resultado": { "b": 2 }
                        }
                        """.formatted(nomeUnico()))
                .when().post("/raiox/simulacoes")
                .then().statusCode(401);
    }

    @Test
    void buscarPorIdDeveRetornarSimulacaoComFormularioEResultado() {
        String nome = nomeUnico();
        String id = criarSimulacao(nome, "Alface");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/simulacoes/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo(nome))
                .body("formulario.produtoSelecionado", equalTo("Alface"))
                .body("resultado.mensagem", equalTo("Preço acima do mínimo."));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/simulacoes/{id}", "00000000-0000-0000-0000-000000000000")
                .then().statusCode(404)
                .body("message", equalTo("Simulação não encontrada"));
    }

    @Test
    void excluirDeveRemoverASimulacao() {
        String id = criarSimulacao(nomeUnico(), "Alface");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().delete("/raiox/simulacoes/{id}", id)
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/simulacoes/{id}", id)
                .then().statusCode(404);
    }

    // ---------------------------------------------------------- listagem

    @Test
    void listarSemSimulacaoDeveTrazerPaginaVazia() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("conteudo", is(empty()))
                .body("total", is(0));
    }

    @Test
    void listarSemSortDeveTrazerMaisRecentePrimeiro() throws InterruptedException {
        String primeira = criarSimulacao("Aaa " + System.nanoTime(), "Alface");
        Thread.sleep(5);
        String segunda = criarSimulacao("Zzz " + System.nanoTime(), "Alface");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("conteudo[0].id", equalTo(segunda))
                .body("conteudo[1].id", equalTo(primeira));
    }

    @Test
    void listarComSortNomeDeveSobreporAoPadraoDeRecencia() throws InterruptedException {
        String primeira = criarSimulacao("Aaa " + System.nanoTime(), "Alface");
        Thread.sleep(5);
        String segunda = criarSimulacao("Zzz " + System.nanoTime(), "Alface");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("sort", "nome,asc")
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("conteudo[0].id", equalTo(primeira))
                .body("conteudo[1].id", equalTo(segunda));
    }

    @Test
    void listarDevePaginarESerFiltradaPorTermo() {
        String marcador = "Zzz" + System.nanoTime();
        String idA = criarSimulacao(marcador + " A", "Alface");
        String idB = criarSimulacao(marcador + " B", "Alface");
        String idC = criarSimulacao(marcador + " C", "Alface");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", marcador)
                .queryParam("sort", "nome,asc")
                .queryParam("offset", 0)
                .queryParam("limit", 2)
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(2))
                .body("conteudo[0].id", equalTo(idA))
                .body("conteudo[1].id", equalTo(idB));

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", marcador)
                .queryParam("sort", "nome,asc")
                .queryParam("offset", 2)
                .queryParam("limit", 2)
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("conteudo.size()", is(1))
                .body("conteudo[0].id", equalTo(idC));
    }

    @Test
    void listarComTermoNoProdutoDeveEncontrar() {
        String produto = "Produto" + System.nanoTime();
        criarSimulacao(nomeUnico(), produto);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", produto)
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("total", is(1));
    }

    @Test
    void listarComSortInvalidoDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("sort", "precoMinimo,asc")
                .when().get("/raiox/simulacoes")
                .then().statusCode(400)
                .body("message", equalTo("Campo de ordenação inválido: precoMinimo"));
    }

    @Test
    void listarNaoDeveTrazerSimulacaoDeOutroProdutor() {
        String nomeDoOutro = nomeUnico();
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);

        given()
                .header("Authorization", "Bearer " + outroAdmin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "modulo": "VEGETAL",
                          "produto": "Alface",
                          "semaforo": "GREEN",
                          "formulario": { "a": 1 },
                          "resultado": { "b": 2 },
                          "produtorId": "%s"
                        }
                        """.formatted(nomeDoOutro, outroAdmin.produtorId()))
                .when().post("/raiox/simulacoes")
                .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", nomeDoOutro)
                .when().get("/raiox/simulacoes")
                .then().statusCode(200)
                .body("total", is(0));
    }
}
