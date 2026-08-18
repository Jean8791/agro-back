package br.com.codenest.agro.usuario;

import static io.restassured.RestAssured.given;
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
class UsuarioResourceTest {

    @Inject
    JwtTestSupport jwt;

    /** Um produtor próprio por teste: nenhum caso enxerga o dado do outro. */
    private JwtTestSupport.Acesso admin;

    @BeforeEach
    void prepararAcesso() {
        admin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
    }

    private String emailUnico() {
        return "teste." + System.nanoTime() + "@teste.com";
    }

    private String criarUsuario(String email) {
        return criarUsuarioCom(admin.token(), email);
    }

    private String criarUsuarioCom(String token, String email) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Usuário Teste",
                          "email": "%s",
                          "senha": "senha123",
                          "papelId": "01890000-0000-7000-8000-000000000012"
                        }
                        """.formatted(email))
                .when().post("/usuarios")
                .then().statusCode(201)
                .body("email", equalTo(email))
                .body("ativo", is(true))
                .body("papelNome", equalTo("Consulta"))
                .extract().path("id");
    }

    @Test
    void criarDeveRetornar201() {
        criarUsuario(emailUnico());
    }

    @Test
    void criarComEmailDuplicadoDeveRetornar400() {
        String email = emailUnico();
        criarUsuario(email);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Outro Usuário",
                          "email": "%s",
                          "senha": "senha123",
                          "papelId": "01890000-0000-7000-8000-000000000012"
                        }
                        """.formatted(email))
                .when().post("/usuarios")
                .then().statusCode(400)
                .body("message", equalTo("E-mail já cadastrado"));
    }

    @Test
    void criarSemTokenDeveRetornar401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Sem Token",
                          "email": "%s",
                          "senha": "senha123",
                          "papelId": "01890000-0000-7000-8000-000000000012"
                        }
                        """.formatted(emailUnico()))
                .when().post("/usuarios")
                .then().statusCode(401);
    }

    @Test
    void listarDeveTrazerApenasUsuariosDoProprioProdutor() {
        String email = emailUnico();
        criarUsuario(email);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios")
                .then().statusCode(200)
                .body("conteudo.findAll { it.email == '%s' }.size()".formatted(email), is(1));
    }

    @Test
    void buscarPorIdDeveRetornarUsuario() {
        String email = emailUnico();
        String id = criarUsuario(email);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios/{id}", id)
                .then().statusCode(200)
                .body("email", equalTo(email));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() {
        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios/{id}", "00000000-0000-0000-0000-000000000000")
                .then().statusCode(404)
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    void editarDeveAtualizarNomeEPapel() {
        String id = criarUsuario(emailUnico());
        String novoEmail = emailUnico();

        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Nome Editado",
                          "email": "%s",
                          "papelId": "01890000-0000-7000-8000-000000000011"
                        }
                        """.formatted(novoEmail))
                .when().put("/usuarios/{id}", id)
                .then().statusCode(200)
                .body("nome", equalTo("Nome Editado"))
                .body("email", equalTo(novoEmail))
                .body("papelNome", equalTo("Operacional"));
    }

    @Test
    void editarComEmailDeOutroUsuarioDeveRetornar400() {
        String emailExistente = emailUnico();
        criarUsuario(emailExistente);
        String id = criarUsuario(emailUnico());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Tentando Duplicar",
                          "email": "%s",
                          "papelId": "01890000-0000-7000-8000-000000000012"
                        }
                        """.formatted(emailExistente))
                .when().put("/usuarios/{id}", id)
                .then().statusCode(400)
                .body("message", equalTo("E-mail já cadastrado"));
    }

    @Test
    void bloquearEDesbloquearDeveAlternarOAtivo() {
        String id = criarUsuario(emailUnico());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().patch("/usuarios/{id}/bloquear", id)
                .then().statusCode(200)
                .body("ativo", is(false));

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().patch("/usuarios/{id}/desbloquear", id)
                .then().statusCode(200)
                .body("ativo", is(true));
    }

    @Test
    void excluirDeveInativarOUsuario() {
        String id = criarUsuario(emailUnico());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().delete("/usuarios/{id}", id)
                .then().statusCode(204);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios/{id}", id)
                .then().statusCode(200)
                .body("ativo", is(false));
    }

    // ---------------------------------------------------------- isolamento

    @Test
    void usuarioDeOutroProdutorNaoApareceNaListagem() {
        String emailDoOutro = emailUnico();
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
        criarUsuarioCom(outroAdmin.token(), emailDoOutro);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios")
                .then().statusCode(200)
                .body("conteudo.findAll { it.email == '%s' }.size()".formatted(emailDoOutro), is(0));
    }

    @Test
    void buscarUsuarioDeOutroProdutorPeloIdDeveRetornar404() {
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
        String idDoOutro = criarUsuarioCom(outroAdmin.token(), emailUnico());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios/{id}", idDoOutro)
                .then().statusCode(404)
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    void excluirUsuarioDeOutroProdutorDeveRetornar404() {
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
        String idDoOutro = criarUsuarioCom(outroAdmin.token(), emailUnico());

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().delete("/usuarios/{id}", idDoOutro)
                .then().statusCode(404);
    }

    @Test
    void informarProdutorDeOutroNaQueryNaoDeveVazarDados() {
        String emailDoOutro = emailUnico();
        var outroAdmin = jwt.tokenDoPapel(PapeisDeSistema.ADMINISTRADOR);
        criarUsuarioCom(outroAdmin.token(), emailDoOutro);

        // o parâmetro só vale para super admin; aqui tem que ser ignorado
        given()
                .header("Authorization", "Bearer " + admin.token())
                .queryParam("produtorId", outroAdmin.produtorId().toString())
                .when().get("/usuarios")
                .then().statusCode(200)
                .body("conteudo.findAll { it.email == '%s' }.size()".formatted(emailDoOutro), is(0));
    }
}
