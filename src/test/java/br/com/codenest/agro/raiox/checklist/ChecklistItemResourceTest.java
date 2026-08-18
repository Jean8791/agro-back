package br.com.codenest.agro.raiox.checklist;

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
class ChecklistItemResourceTest {

    @Inject
    JwtTestSupport jwt;

    /** Um produtor próprio por teste: nenhum caso enxerga o dado do outro. */
    private JwtTestSupport.Acesso admin;

    @BeforeEach
    void prepararAcesso() {
        admin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
    }

    private String rotuloUnico() {
        return "Custo Teste " + System.nanoTime();
    }

    private String criarItem(String modulo, String rotulo) {
        return given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "%s",
                          "rotulo": "%s",
                          "nota": "Nota de teste",
                          "produtorId": "%s"
                        }
                        """.formatted(modulo, rotulo, admin.produtorId()))
                .when().post("/raiox/checklist")
                .then().statusCode(201)
                .body("rotulo", equalTo(rotulo))
                .body("modulo", equalTo(modulo))
                .extract().path("id");
    }

    @Test
    void criarDeveRetornar201() {
        criarItem("VEGETAL", rotuloUnico());
    }

    @Test
    void criarSemTokenDeveRetornar401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "VEGETAL",
                          "rotulo": "%s"
                        }
                        """.formatted(rotuloUnico()))
                .when().post("/raiox/checklist")
                .then().statusCode(401);
    }

    @Test
    void buscarPorIdDeveRetornarItem() {
        String rotulo = rotuloUnico();
        String id = criarItem("ANIMAL", rotulo);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/checklist/{id}", id)
                .then().statusCode(200)
                .body("rotulo", equalTo(rotulo));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/checklist/{id}", "00000000-0000-0000-0000-000000000000")
                .then().statusCode(404)
                .body("message", equalTo("Item de checklist não encontrado"));
    }

    @Test
    void editarDeveAtualizarItem() {
        String id = criarItem("VEGETAL", rotuloUnico());
        String novoRotulo = rotuloUnico();

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "AMBOS",
                          "rotulo": "%s",
                          "nota": "Editado"
                        }
                        """.formatted(novoRotulo))
                .when().put("/raiox/checklist/{id}", id)
                .then().statusCode(200)
                .body("rotulo", equalTo(novoRotulo))
                .body("modulo", equalTo("AMBOS"));
    }

    @Test
    void excluirDeveRemoverOItem() {
        String id = criarItem("VEGETAL", rotuloUnico());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().delete("/raiox/checklist/{id}", id)
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/checklist/{id}", id)
                .then().statusCode(404);
    }

    // ---------------------------------------------------------- listagem

    @Test
    void listarSemItemCadastradoDeveTrazerPaginaVazia() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/checklist")
                .then().statusCode(200)
                .body("conteudo", is(empty()))
                .body("total", is(0));
    }

    @Test
    void listarDeveAgruparPorModuloAntesDoRotulo() {
        criarItem("VEGETAL", "Zzz " + System.nanoTime());
        criarItem("AMBOS", "Aaa " + System.nanoTime());
        criarItem("ANIMAL", "Mmm " + System.nanoTime());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("sort", "rotulo,asc")
                .queryParam("limit", 10)
                .when().get("/raiox/checklist")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo[0].modulo", equalTo("AMBOS"))
                .body("conteudo[1].modulo", equalTo("ANIMAL"))
                .body("conteudo[2].modulo", equalTo("VEGETAL"));
    }

    @Test
    void listarDevePaginarESerFiltradaPorTermo() {
        String marcador = "Zzz" + System.nanoTime();
        String idA = criarItem("VEGETAL", marcador + " A");
        String idB = criarItem("VEGETAL", marcador + " B");
        String idC = criarItem("VEGETAL", marcador + " C");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", marcador)
                .queryParam("sort", "rotulo,asc")
                .queryParam("offset", 0)
                .queryParam("limit", 2)
                .when().get("/raiox/checklist")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(2))
                .body("conteudo[0].id", equalTo(idA))
                .body("conteudo[1].id", equalTo(idB));

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", marcador)
                .queryParam("sort", "rotulo,asc")
                .queryParam("offset", 2)
                .queryParam("limit", 2)
                .when().get("/raiox/checklist")
                .then().statusCode(200)
                .body("conteudo.size()", is(1))
                .body("conteudo[0].id", equalTo(idC));
    }

    @Test
    void listarComSortInvalidoDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("sort", "nota,asc")
                .when().get("/raiox/checklist")
                .then().statusCode(400)
                .body("message", equalTo("Campo de ordenação inválido: nota"));
    }

    @Test
    void listarNaoDeveTrazerItemDeOutroProdutor() {
        String rotuloDoOutro = rotuloUnico();
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);

        given()
                .header("Authorization", "Bearer " + outroAdmin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "VEGETAL",
                          "rotulo": "%s",
                          "produtorId": "%s"
                        }
                        """.formatted(rotuloDoOutro, outroAdmin.produtorId()))
                .when().post("/raiox/checklist")
                .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", rotuloDoOutro)
                .when().get("/raiox/checklist")
                .then().statusCode(200)
                .body("total", is(0));
    }
}
