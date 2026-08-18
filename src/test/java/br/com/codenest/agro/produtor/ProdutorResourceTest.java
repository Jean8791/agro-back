package br.com.codenest.agro.produtor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import br.com.codenest.agro.shared.JwtTestSupport;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * `produtor` não tem escopo de tenant — é ele quem define o tenant — então a
 * listagem enxerga tudo que qualquer teste (ou execução anterior, já que o
 * Postgres de teste não é recriado a cada run) já criou. Os testes de
 * paginação por isso sempre filtram por um marcador único (`termo`) para
 * trabalhar só com os registros que o próprio teste criou.
 */
@QuarkusTest
class ProdutorResourceTest {

    @Inject
    JwtTestSupport jwt;

    private String superAdmin;

    @BeforeEach
    void prepararAcesso() {
        superAdmin = jwt.tokenSuperAdmin();
    }

    private String documentoUnico() {
        long numero = ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L);
        return String.valueOf(numero);
    }

    private String marcadorUnico() {
        return "Marcador" + System.nanoTime();
    }

    private String criarProdutor(String nome, String documento) {
        return given()
                .header("Authorization", "Bearer " + superAdmin)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "documento": "%s",
                          "tipoPessoa": "PJ"
                        }
                        """.formatted(nome, documento))
                .when().post("/produtores")
                .then().statusCode(201)
                .body("nome", equalTo(nome))
                .body("documento", equalTo(documento))
                .body("ativo", is(true))
                .extract().path("id");
    }

    @Test
    void criarDeveRetornar201() {
        criarProdutor("Fazenda " + marcadorUnico(), documentoUnico());
    }

    @Test
    void criarComDocumentoDuplicadoDeveRetornar400() {
        String documento = documentoUnico();
        criarProdutor("Fazenda " + marcadorUnico(), documento);

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Outra Fazenda",
                          "documento": "%s",
                          "tipoPessoa": "PJ"
                        }
                        """.formatted(documento))
                .when().post("/produtores")
                .then().statusCode(400)
                .body("message", equalTo("Documento já cadastrado"));
    }

    @Test
    void criarComDocumentoInvalidoDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + superAdmin)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Fazenda Documento Ruim",
                          "documento": "123",
                          "tipoPessoa": "PJ"
                        }
                        """)
                .when().post("/produtores")
                .then().statusCode(400);
    }

    @Test
    void criarSemTokenDeveRetornar401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Sem Token",
                          "documento": "%s",
                          "tipoPessoa": "PJ"
                        }
                        """.formatted(documentoUnico()))
                .when().post("/produtores")
                .then().statusCode(401);
    }

    @Test
    void criarSemSerSuperAdminDeveRetornar403() {
        String tokenAdmin = jwt.tokenAdmin();

        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Fazenda de Quem Não Pode",
                          "documento": "%s",
                          "tipoPessoa": "PJ"
                        }
                        """.formatted(documentoUnico()))
                .when().post("/produtores")
                .then().statusCode(403);
    }

    @Test
    void buscarPorIdDeveRetornarProdutor() {
        String nome = "Fazenda " + marcadorUnico();
        String id = criarProdutor(nome, documentoUnico());

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .when().get("/produtores/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo(nome));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() {
        given()
                .header("Authorization", "Bearer " + superAdmin)
                .when().get("/produtores/{id}", "00000000-0000-0000-0000-000000000000")
                .then().statusCode(404)
                .body("message", equalTo("Produtor não encontrado"));
    }

    @Test
    void editarDeveAtualizarNomeEDocumento() {
        String id = criarProdutor("Fazenda " + marcadorUnico(), documentoUnico());
        String novoNome = "Fazenda Editada " + marcadorUnico();
        String novoDocumento = documentoUnico();

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "%s",
                          "documento": "%s",
                          "tipoPessoa": "PF"
                        }
                        """.formatted(novoNome, novoDocumento))
                .when().put("/produtores/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo(novoNome))
                .body("documento", equalTo(novoDocumento))
                .body("tipoPessoa", equalTo("PF"));
    }

    @Test
    void inativarEReativarDeveAlternarAtivo() {
        String id = criarProdutor("Fazenda " + marcadorUnico(), documentoUnico());

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .when().patch("/produtores/{id}/inativar", id)
                .then().statusCode(200)
                .body("ativo", is(false));

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .when().patch("/produtores/{id}/reativar", id)
                .then().statusCode(200)
                .body("ativo", is(true));
    }

    // ---------------------------------------------------------- listagem

    @Test
    void listarComTermoSemCorrespondenciaDeveTrazerPaginaVazia() {
        given()
                .header("Authorization", "Bearer " + superAdmin)
                .queryParam("termo", "não-existe-" + System.nanoTime())
                .when().get("/produtores")
                .then().statusCode(200)
                .body("conteudo", is(empty()))
                .body("total", is(0));
    }

    @Test
    void listarDevePaginarESerFiltradaPorTermo() {
        String marcador = marcadorUnico();
        String idA = criarProdutor(marcador + " A", documentoUnico());
        String idB = criarProdutor(marcador + " B", documentoUnico());
        String idC = criarProdutor(marcador + " C", documentoUnico());

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .queryParam("termo", marcador)
                .queryParam("sort", "nome,asc")
                .queryParam("offset", 0)
                .queryParam("limit", 2)
                .when().get("/produtores")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(2))
                .body("conteudo[0].id", equalTo(idA))
                .body("conteudo[1].id", equalTo(idB));

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .queryParam("termo", marcador)
                .queryParam("sort", "nome,asc")
                .queryParam("offset", 2)
                .queryParam("limit", 2)
                .when().get("/produtores")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(1))
                .body("conteudo[0].id", equalTo(idC));
    }

    @Test
    void listarComTermoNoDocumentoDeveEncontrar() {
        String documento = documentoUnico();
        criarProdutor("Fazenda " + marcadorUnico(), documento);

        given()
                .header("Authorization", "Bearer " + superAdmin)
                .queryParam("termo", documento)
                .when().get("/produtores")
                .then().statusCode(200)
                .body("total", is(1))
                .body("conteudo[0].documento", equalTo(documento));
    }

    @Test
    void listarComSortInvalidoDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + superAdmin)
                .queryParam("sort", "documento,asc")
                .when().get("/produtores")
                .then().statusCode(400)
                .body("message", equalTo("Campo de ordenação inválido: documento"));
    }
}
