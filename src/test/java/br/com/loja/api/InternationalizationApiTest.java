package br.com.loja.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import br.com.loja.repository.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InternationalizationApiTest {

    private static final String BASE = "/api/v1/customers";

    @LocalServerPort
    int port;

    @Autowired
    CustomerRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }

    private static Map<String, Object> customer(String cpf, String email) {
        Map<String, Object> address = new HashMap<>();
        address.put("zipCode", "29101-000");
        address.put("street", "Avenida Principal");
        address.put("number", "100");
        address.put("neighborhood", "Centro");
        address.put("city", "Vila Velha");
        address.put("state", "ES");
        Map<String, Object> c = new HashMap<>();
        c.put("fullName", "Maria da Silva");
        c.put("cpf", cpf);
        c.put("email", email);
        c.put("phoneNumber", "(27) 99999-9999");
        c.put("birthDate", "1990-05-15");
        c.put("address", address);
        return c;
    }

    private Response get(String path, String language) {
        var request = given();
        if (language != null) {
            request = request.header("Accept-Language", language);
        }
        return request.get(path);
    }

    private Response post(Object body, String language) {
        var request = given().contentType(ContentType.JSON).body(body);
        if (language != null) {
            request = request.header("Accept-Language", language);
        }
        return request.post(BASE);
    }

    @Test
    void portugueseIsReturnedForPtBr() {
        get(BASE + "/999", "pt-BR").then().statusCode(404)
                .body("message", equalTo("Cliente não encontrado."));
    }

    @Test
    void englishIsReturnedForEnUs() {
        get(BASE + "/999", "en-US").then().statusCode(404)
                .body("message", equalTo("Customer not found."));
    }

    @Test
    void portugueseIsTheDefaultWithoutHeader() {
        get(BASE + "/999", null).then().statusCode(404)
                .body("message", equalTo("Cliente não encontrado."));
    }

    @Test
    void unsupportedLanguageFallsBackToPortuguese() {
        get(BASE + "/999", "fr-FR").then().statusCode(404)
                .body("message", equalTo("Cliente não encontrado."));
    }

    @Test
    void qualityListsAreHonored() {
        get(BASE + "/999", "es-ES,en-US;q=0.8").then().body("message", equalTo("Customer not found."));
    }

    @Test
    void successMessageIsTranslated() {
        post(customer("529.982.247-25", "a@example.com"), "pt-BR").then().statusCode(201)
                .body("message", equalTo("Cliente cadastrado com sucesso."));
        post(customer("111.444.777-35", "b@example.com"), "en-US").then().statusCode(201)
                .body("message", equalTo("Customer successfully registered."));
    }

    @Test
    void validationMessagesAreTranslated() {
        Map<String, Object> body = customer("529.982.247-25", "a@example.com");
        body.remove("fullName");
        post(body, "pt-BR").then().statusCode(400)
                .body("code", equalTo("VALIDATION_ERROR"))
                .body("message", equalTo("Falha na validação."))
                .body("errors.find { it.field == 'fullName' }.message", equalTo("O nome completo é obrigatório."));
        post(body, "en-US").then().statusCode(400)
                .body("message", equalTo("Validation failed."))
                .body("errors.find { it.field == 'fullName' }.message", equalTo("Full name is required."));
    }

    @Test
    void conflictAndErrorsKeepCodesAndStructureAcrossLanguages() {
        post(customer("529.982.247-25", "a@example.com"), "en-US").then().statusCode(201);

        Response pt = post(customer("529.982.247-25", "c@example.com"), "pt-BR");
        Response en = post(customer("529.982.247-25", "c@example.com"), "en-US");
        pt.then().statusCode(409).body("code", equalTo("CUSTOMER_ALREADY_EXISTS"));
        en.then().statusCode(409).body("code", equalTo("CUSTOMER_ALREADY_EXISTS"));
        assertEquals(keys(pt), keys(en));
        assertEquals(pt.<String>path("errors[0].field"), en.<String>path("errors[0].field"));

        Response ptNotFound = get(BASE + "/999", "pt-BR");
        Response enNotFound = get(BASE + "/999", "en-US");
        assertEquals(keys(ptNotFound), keys(enNotFound));
        assertEquals(ptNotFound.<String>path("code"), enNotFound.<String>path("code"));
        assertEquals("/api/v1/customers/999", enNotFound.<String>path("path"));
    }

    @Test
    void malformedBodyReturnsInvalidRequest() {
        given().contentType(ContentType.JSON).body("{bad").header("Accept-Language", "en-US")
                .post(BASE).then().statusCode(400).body("code", equalTo("INVALID_REQUEST"));
    }

    private static TreeSet<String> keys(Response response) {
        Map<String, Object> map = response.jsonPath().getMap("$");
        return new TreeSet<>(map.keySet());
    }
}
