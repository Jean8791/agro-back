package br.com.codenest.agro.raiox.produto;

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
class ProdutoBaseResourceTest {

    @Inject
    JwtTestSupport jwt;

    /** Um produtor próprio por teste: nenhum caso enxerga o dado do outro. */
    private JwtTestSupport.Acesso admin;

    @BeforeEach
    void prepararAcesso() {
        admin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
    }

    private String nomeUnico() {
        return "Produto Teste " + System.nanoTime();
    }

    private String criarProduto(String produto, String categoria) {
        return given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "VEGETAL",
                          "categoria": "%s",
                          "produto": "%s",
                          "unidadeTecnica": "kg",
                          "unidadeComercial": "caixa",
                          "quantidadePorUnidade": 20,
                          "unidadePreco": "R$/caixa",
                          "produtorId": "%s"
                        }
                        """.formatted(categoria, produto, admin.produtorId()))
                .when().post("/raiox/produtos")
                .then().statusCode(201)
                .body("produto", equalTo(produto))
                .body("categoria", equalTo(categoria))
                .extract().path("id");
    }

    @Test
    void criarDeveRetornar201() {
        criarProduto(nomeUnico(), "Fruta");
    }

    @Test
    void criarComQuantidadeInvalidaDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "VEGETAL",
                          "categoria": "Fruta",
                          "produto": "%s",
                          "unidadeTecnica": "kg",
                          "unidadeComercial": "caixa",
                          "quantidadePorUnidade": 0,
                          "unidadePreco": "R$/caixa",
                          "produtorId": "%s"
                        }
                        """.formatted(nomeUnico(), admin.produtorId()))
                .when().post("/raiox/produtos")
                .then().statusCode(400);
    }

    @Test
    void criarSemTokenDeveRetornar401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "VEGETAL",
                          "categoria": "Fruta",
                          "produto": "%s",
                          "unidadeTecnica": "kg",
                          "unidadeComercial": "caixa",
                          "quantidadePorUnidade": 20,
                          "unidadePreco": "R$/caixa"
                        }
                        """.formatted(nomeUnico()))
                .when().post("/raiox/produtos")
                .then().statusCode(401);
    }

    @Test
    void buscarPorIdDeveRetornarProduto() {
        String produto = nomeUnico();
        String id = criarProduto(produto, "Fruta");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/produtos/{id}", id)
                .then().statusCode(200)
                .body("produto", equalTo(produto));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/produtos/{id}", "00000000-0000-0000-0000-000000000000")
                .then().statusCode(404)
                .body("message", equalTo("Produto não encontrado"));
    }

    @Test
    void editarDeveAtualizarProduto() {
        String id = criarProduto(nomeUnico(), "Fruta");
        String novoNome = nomeUnico();

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "ANIMAL",
                          "categoria": "Bovino",
                          "produto": "%s",
                          "unidadeTecnica": "kg",
                          "unidadeComercial": "arroba",
                          "quantidadePorUnidade": 15,
                          "unidadePreco": "R$/arroba",
                          "tipoProducaoAnimal": "CONTINUA"
                        }
                        """.formatted(novoNome))
                .when().put("/raiox/produtos/{id}", id)
                .then().statusCode(200)
                .body("produto", equalTo(novoNome))
                .body("modulo", equalTo("ANIMAL"));
    }

    @Test
    void excluirDeveRemoverOProduto() {
        String id = criarProduto(nomeUnico(), "Fruta");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().delete("/raiox/produtos/{id}", id)
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/produtos/{id}", id)
                .then().statusCode(404);
    }

    // ---------------------------------------------------------- listagem

    @Test
    void listarSemProdutoCadastradoDeveTrazerPaginaVazia() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .when().get("/raiox/produtos")
                .then().statusCode(200)
                .body("conteudo", is(empty()))
                .body("total", is(0));
    }

    @Test
    void listarDevePaginarESerFiltradaPorTermo() {
        String marcador = "Zzz" + System.nanoTime();
        String idA = criarProduto(marcador + " A", "Categoria");
        String idB = criarProduto(marcador + " B", "Categoria");
        String idC = criarProduto(marcador + " C", "Categoria");

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", marcador)
                .queryParam("sort", "produto,asc")
                .queryParam("offset", 0)
                .queryParam("limit", 2)
                .when().get("/raiox/produtos")
                .then().statusCode(200)
                .body("total", is(3))
                .body("conteudo.size()", is(2))
                .body("conteudo[0].id", equalTo(idA))
                .body("conteudo[1].id", equalTo(idB));

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", marcador)
                .queryParam("sort", "produto,desc")
                .queryParam("offset", 0)
                .queryParam("limit", 2)
                .when().get("/raiox/produtos")
                .then().statusCode(200)
                .body("conteudo[0].id", equalTo(idC));
    }

    @Test
    void listarComTermoNaCategoriaDeveEncontrar() {
        String categoria = "Categoria" + System.nanoTime();
        criarProduto(nomeUnico(), categoria);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", categoria)
                .when().get("/raiox/produtos")
                .then().statusCode(200)
                .body("total", is(1));
    }

    @Test
    void listarComSortInvalidoDeveRetornar400() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("sort", "categoria,asc")
                .when().get("/raiox/produtos")
                .then().statusCode(400)
                .body("message", equalTo("Campo de ordenação inválido: categoria"));
    }

    @Test
    void listarNaoDeveTrazerProdutoDeOutroProdutor() {
        String produtoDoOutro = nomeUnico();
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);

        given()
                .header("Authorization", "Bearer " + outroAdmin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "modulo": "VEGETAL",
                          "categoria": "Fruta",
                          "produto": "%s",
                          "unidadeTecnica": "kg",
                          "unidadeComercial": "caixa",
                          "quantidadePorUnidade": 20,
                          "unidadePreco": "R$/caixa",
                          "produtorId": "%s"
                        }
                        """.formatted(produtoDoOutro, outroAdmin.produtorId()))
                .when().post("/raiox/produtos")
                .then().statusCode(201);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", admin.produtorId().toString())
                .queryParam("termo", produtoDoOutro)
                .when().get("/raiox/produtos")
                .then().statusCode(200)
                .body("total", is(0));
    }
}
