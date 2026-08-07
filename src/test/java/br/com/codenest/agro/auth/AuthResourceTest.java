package br.com.codenest.agro.auth;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.codenest.agro.produtor.enums.Papel;
import br.com.codenest.agro.shared.JwtTestSupport;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AuthResourceTest {

    private static final String SENHA = "senha123";
    private static final AtomicInteger SEQUENCIA_IP = new AtomicInteger();

    @Inject
    JwtTestSupport jwt;

    private JwtTestSupport.Acesso admin;

    // JUnit cria uma instância por teste, então cada caso tem seu próprio IP e
    // não herda as falhas contadas pelos outros (o profile %test habilita o
    // proxy-address-forwarding que faz o X-Forwarded-For valer)
    private final String ip = ipUnico();

    @BeforeEach
    void prepararAcesso() {
        admin = jwt.tokenDoPapel(Papel.ADMIN);
    }

    private static String ipUnico() {
        int n = SEQUENCIA_IP.incrementAndGet();
        long semente = System.nanoTime();
        return "10.%d.%d.%d".formatted((semente >> 12) & 0xFF, (semente >> 4) & 0xFF, n & 0xFF);
    }

    private RequestSpecification requisicaoDeLogin(String email, String senha) {
        return given()
                .header("X-Forwarded-For", ip)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "senha": "%s"
                        }
                        """.formatted(email, senha));
    }

    private String criarUsuario() {
        String email = "login." + System.nanoTime() + "@teste.com";

        given()
                .header("Authorization", "Bearer " + admin.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Usuário Login",
                          "email": "%s",
                          "senha": "%s",
                          "papel": "CONSULTA"
                        }
                        """.formatted(email, SENHA))
                .when().post("/usuarios")
                .then().statusCode(201);

        return email;
    }

    private String login(String email) {
        return requisicaoDeLogin(email, SENHA)
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("refreshToken");
    }

    private void falharLogin(String email) {
        requisicaoDeLogin(email, "senha-errada")
                .when().post("/auth/login")
                .then().statusCode(400);
    }

    private String idPorEmail(String email) {
        return given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios")
                .then().statusCode(200)
                .extract().path("find { it.email == '%s' }.id".formatted(email));
    }

    @Test
    void loginComCredenciaisValidasDeveRetornarToken() {
        String email = criarUsuario();

        requisicaoDeLogin(email, SENHA)
                .when().post("/auth/login")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("expiresIn", notNullValue())
                .body("refreshToken", notNullValue())
                .body("refreshExpiresIn", notNullValue());
    }

    @Test
    void loginComSenhaInvalidaDeveRetornar400() {
        String email = criarUsuario();

        requisicaoDeLogin(email, "senha-errada")
                .when().post("/auth/login")
                .then().statusCode(400)
                .body("message", equalTo("Credenciais inválidas"));
    }

    @Test
    void loginComEmailInexistenteDeveRetornar400() {
        requisicaoDeLogin("nao.existe.%s@teste.com".formatted(System.nanoTime()), SENHA)
                .when().post("/auth/login")
                .then().statusCode(400)
                .body("message", equalTo("Credenciais inválidas"));
    }

    @Test
    void loginComUsuarioBloqueadoDeveRetornar400() {
        String email = criarUsuario();
        String id = idPorEmail(email);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().patch("/usuarios/{id}/bloquear", id)
                .then().statusCode(200);

        requisicaoDeLogin(email, SENHA)
                .when().post("/auth/login")
                .then().statusCode(400)
                .body("message", equalTo("Credenciais inválidas"));
    }

    @Test
    void refreshComTokenValidoDeveRetornarNovoAccessToken() {
        String email = criarUsuario();
        String refreshToken = login(email);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when().post("/auth/refresh")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void refreshComTokenJaRotacionadoDeveFalhar() {
        String email = criarUsuario();
        String refreshToken = login(email);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when().post("/auth/refresh")
                .then().statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when().post("/auth/refresh")
                .then().statusCode(400)
                .body("message", equalTo("Sessão inválida ou expirada"));
    }

    @Test
    void refreshComTokenInexistenteDeveFalhar() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "token-que-nunca-existiu"
                        }
                        """)
                .when().post("/auth/refresh")
                .then().statusCode(400)
                .body("message", equalTo("Sessão inválida ou expirada"));
    }

    @Test
    void logoutSeguidoDeRefreshDeveFalhar() {
        String email = criarUsuario();
        String refreshToken = login(email);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when().post("/auth/logout")
                .then().statusCode(204);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when().post("/auth/refresh")
                .then().statusCode(400)
                .body("message", equalTo("Sessão inválida ou expirada"));
    }

    @Test
    void refreshAposBloquearUsuarioDeveFalhar() {
        String email = criarUsuario();
        String refreshToken = login(email);
        String id = idPorEmail(email);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().patch("/usuarios/{id}/bloquear", id)
                .then().statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when().post("/auth/refresh")
                .then().statusCode(400)
                .body("message", equalTo("Sessão inválida ou expirada"));
    }

    // ------------------------------------------------------------ multi-tenant

    @Test
    void tokenDeLoginDeveCarregarProdutorEPapel() {
        String email = criarUsuario();

        String token =
                requisicaoDeLogin(email, SENHA)
                        .when().post("/auth/login")
                        .then().statusCode(200)
                        .extract().path("token");

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), UTF_8);

        assertTrue(
                payload.contains("\"%s\"".formatted(admin.produtorId())),
                "o token precisa levar o produtor do vínculo: " + payload);
        assertTrue(payload.contains("CONSULTA"), "o token precisa levar o papel: " + payload);
    }

    // ------------------------------------------------------------- rate limit

    @Test
    void excederTentativasDoMesmoEmailDeveRetornar429() {
        String email = criarUsuario();

        for (int i = 0; i < 5; i++) {
            falharLogin(email);
        }

        requisicaoDeLogin(email, "senha-errada")
                .when().post("/auth/login")
                .then().statusCode(429)
                .body("message", startsWith("Muitas tentativas de login"));
    }

    @Test
    void bloqueioPorTentativasNaoDeveInativarOUsuario() {
        String email = criarUsuario();
        String id = idPorEmail(email);

        for (int i = 0; i < 5; i++) {
            falharLogin(email);
        }

        requisicaoDeLogin(email, SENHA)
                .when().post("/auth/login")
                .then().statusCode(429);

        given()
                .header("Authorization", "Bearer " + admin.token())
                .when().get("/usuarios/{id}", id)
                .then().statusCode(200)
                .body("ativo", equalTo(true));
    }

    @Test
    void loginComSucessoDeveZerarContadorDeTentativas() {
        String email = criarUsuario();

        for (int i = 0; i < 4; i++) {
            falharLogin(email);
        }

        login(email);

        // com o contador zerado, as próximas falhas voltam a caber na janela
        for (int i = 0; i < 4; i++) {
            falharLogin(email);
        }
    }

    @Test
    void varrerVariosEmailsDoMesmoIpDeveRetornar429() {
        for (int i = 0; i < 20; i++) {
            falharLogin("varredura.%d.%s@teste.com".formatted(i, System.nanoTime()));
        }

        requisicaoDeLogin("varredura.final.%s@teste.com".formatted(System.nanoTime()), SENHA)
                .when().post("/auth/login")
                .then().statusCode(429)
                .body("message", startsWith("Muitas tentativas de login"));
    }

    // ------------------------------------------------------- config do rate limit

    @Test
    void buscarRateLimitDeveRetornarOsLimitesVigentes() {
        given()
                .header("Authorization", "Bearer " + jwt.tokenSuperAdmin())
                .when().get("/auth/rate-limit")
                .then().statusCode(200)
                .body("maxTentativasEmail", equalTo(5))
                .body("maxTentativasIp", equalTo(20))
                .body("janelaMinutos", equalTo(15))
                .body("bloqueioMinutos", equalTo(15));
    }

    @Test
    void buscarRateLimitSemTokenDeveRetornar401() {
        given()
                .when().get("/auth/rate-limit")
                .then().statusCode(401);
    }
}
