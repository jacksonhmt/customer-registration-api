package br.com.loja.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import br.com.loja.repository.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomerApiTest {

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

    private static Map<String, Object> address() {
        Map<String, Object> a = new HashMap<>();
        a.put("zipCode", "29101-000");
        a.put("street", "Avenida Principal");
        a.put("number", "100");
        a.put("complement", "Apartment 604");
        a.put("neighborhood", "Centro");
        a.put("city", "Vila Velha");
        a.put("state", "ES");
        return a;
    }

    private static Map<String, Object> customer(String cpf, String email) {
        Map<String, Object> c = new HashMap<>();
        c.put("fullName", "Maria da Silva");
        c.put("cpf", cpf);
        c.put("email", email);
        c.put("phoneNumber", "(27) 99999-9999");
        c.put("birthDate", "1990-05-15");
        c.put("address", address());
        return c;
    }

    private ValidatableResponse post(Object body) {
        return given().contentType(ContentType.JSON).body(body).post(BASE).then();
    }

    private int createCustomer(String cpf, String email) {
        return post(customer(cpf, email)).statusCode(201).extract().path("data.id");
    }

    @Test
    void registersCustomerWithNormalizedDataAndHidesCpf() {
        post(customer("529.982.247-25", "Maria@Example.com"))
                .statusCode(201)
                .body("success", equalTo(true))
                .body("message", equalTo("Customer successfully registered."))
                .body("data.id", notNullValue())
                .body("data.fullName", equalTo("Maria da Silva"))
                .body("data.email", equalTo("maria@example.com"))
                .body("data.phoneNumber", equalTo("27999999999"))
                .body("data.birthDate", equalTo("1990-05-15"))
                .body("data.createdAt", notNullValue())
                .body("data.address.zipCode", equalTo("29101000"))
                .body("data.address.complement", equalTo("Apartment 604"))
                .body("data.cpf", nullValue());
    }

    @Test
    void registersCustomerWithoutAddress() {
        Map<String, Object> body = customer("52998224725", "a@example.com");
        body.remove("address");
        post(body).statusCode(201).body("data.address", nullValue());
    }

    @Test
    void retrievesCustomerById() {
        int id = createCustomer("52998224725", "a@example.com");

        given().get(BASE + "/" + id).then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Customer successfully retrieved."))
                .body("data.id", equalTo(id))
                .body("data.address.state", equalTo("ES"));
    }

    @Test
    void returnsNotFoundForUnknownCustomer() {
        given().get(BASE + "/999999").then()
                .statusCode(404)
                .body("success", equalTo(false))
                .body("message", equalTo("Customer not found."))
                .body("errors[0].field", equalTo("id"))
                .body("errors[0].message", equalTo("No customer was found with the provided ID."));
    }

    @Test
    void listsCustomersWithPagination() {
        createCustomer("52998224725", "a@example.com");
        createCustomer("11144477735", "b@example.com");
        createCustomer("39053344705", "c@example.com");

        given().queryParam("page", 0).queryParam("size", 2).get(BASE).then()
                .statusCode(200)
                .body("data.content", hasSize(2))
                .body("data.totalElements", equalTo(3))
                .body("data.totalPages", equalTo(2));
        given().queryParam("page", 1).queryParam("size", 2).get(BASE).then()
                .statusCode(200)
                .body("data.content", hasSize(1));
    }

    @Test
    void rejectsInvalidPaginationParameters() {
        given().queryParam("size", 0).get(BASE).then().statusCode(400).body("message", equalTo("Validation error."));
        given().queryParam("size", 101).get(BASE).then().statusCode(400);
        given().queryParam("page", "abc").get(BASE).then().statusCode(400).body("message", equalTo("Invalid request."));
    }

    @Test
    void updatesCustomer() {
        int id = createCustomer("52998224725", "a@example.com");
        Map<String, Object> body = customer("52998224725", "a@example.com");
        body.put("fullName", "Maria Souza");
        body.put("gender", "FEMALE");
        ((Map<String, Object>) body.get("address")).put("city", "Vitoria");

        given().contentType(ContentType.JSON).body(body).put(BASE + "/" + id).then()
                .statusCode(200)
                .body("message", equalTo("Customer successfully updated."))
                .body("data.fullName", equalTo("Maria Souza"))
                .body("data.gender", equalTo("FEMALE"))
                .body("data.address.city", equalTo("Vitoria"))
                .body("data.updatedAt", notNullValue());

        body.remove("address");
        given().contentType(ContentType.JSON).body(body).put(BASE + "/" + id).then()
                .statusCode(200).body("data.address", nullValue());
    }

    @Test
    void updateReturnsNotFoundAndConflict() {
        given().contentType(ContentType.JSON).body(customer("52998224725", "a@example.com"))
                .put(BASE + "/999999").then().statusCode(404);

        createCustomer("52998224725", "a@example.com");
        int other = createCustomer("11144477735", "b@example.com");
        given().contentType(ContentType.JSON).body(customer("52998224725", "b@example.com"))
                .put(BASE + "/" + other).then().statusCode(409).body("errors[0].field", equalTo("cpf"));
    }

    @Test
    void deletesCustomerPhysically() {
        int id = createCustomer("52998224725", "a@example.com");

        given().delete(BASE + "/" + id).then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Customer successfully deleted."));

        given().get(BASE + "/" + id).then().statusCode(404);
        given().delete(BASE + "/" + id).then().statusCode(404);
        given().get(BASE).then().body("data.content", hasSize(0));
        org.assertj.core.api.Assertions.assertThat(repository.findById((long) id)).isEmpty();
    }

    @Test
    void rejectsDuplicateCpfAndEmail() {
        createCustomer("52998224725", "a@example.com");

        post(customer("529.982.247-25", "other@example.com")).statusCode(409)
                .body("success", equalTo(false))
                .body("message", equalTo("Customer already exists."))
                .body("errors[0].field", equalTo("cpf"))
                .body("errors[0].message", equalTo("A customer with this CPF is already registered."));
        post(customer("11144477735", "A@example.com")).statusCode(409)
                .body("errors[0].field", equalTo("email"));
    }

    @Test
    void reportsMissingRequiredFields() {
        post(new HashMap<>()).statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Validation error."))
                .body("errors.field", org.hamcrest.Matchers.hasItems("fullName", "cpf", "email", "phoneNumber", "birthDate"))
                .body("errors.find { it.field == 'fullName' }.message", equalTo("Full name is required."));
    }

    @Test
    void reportsInvalidCpf() {
        post(customer("52998224724", "a@example.com")).statusCode(400)
                .body("errors[0].field", equalTo("cpf"))
                .body("errors[0].message", equalTo("The provided CPF is invalid."));
    }

    @Test
    void validatesFieldBoundaries() {
        Map<String, Object> body = customer("52998224725", "a@example.com");
        body.put("fullName", "Ab");
        body.put("phoneNumber", "123");
        body.put("birthDate", "2999-01-01");
        ((Map<String, Object>) body.get("address")).put("state", "XX");
        ((Map<String, Object>) body.get("address")).put("zipCode", "123");

        post(body).statusCode(400).body("errors.field", org.hamcrest.Matchers.hasItems(
                "fullName", "phoneNumber", "birthDate", "address.state", "address.zipCode"));
    }

    @Test
    void requiresAddressFieldsWhenAddressProvided() {
        Map<String, Object> body = customer("52998224725", "a@example.com");
        body.put("address", new HashMap<>(Map.of("complement", "x")));

        post(body).statusCode(400).body("errors.field", org.hamcrest.Matchers.hasItems(
                "address.zipCode", "address.street", "address.number", "address.neighborhood",
                "address.city", "address.state"));
    }

    @Test
    void reportsInvalidDateFormat() {
        Map<String, Object> body = customer("52998224725", "a@example.com");
        body.put("birthDate", "15/05/1990");

        post(body).statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid request."))
                .body("errors[0].field", equalTo("birthDate"))
                .body("errors[0].message", equalTo("The date must follow the YYYY-MM-DD format."));
    }

    @Test
    void reportsInvalidGenderAndMalformedJson() {
        Map<String, Object> body = customer("52998224725", "a@example.com");
        body.put("gender", "UNKNOWN");
        post(body).statusCode(400).body("message", equalTo("Invalid request."))
                .body("errors[0].field", equalTo("gender"));

        given().contentType(ContentType.JSON).body("{bad json").post(BASE).then()
                .statusCode(400).body("message", equalTo("Invalid request."));
    }

    @Test
    void exposesOpenApiDocumentation() {
        given().get("/v3/api-docs").then().statusCode(200).body("paths", notNullValue());
    }

    @Test
    void unsupportedMethodDoesNotLeakInternals() {
        given().patch(BASE + "/1").then().statusCode(405).body("success", equalTo(false))
                .body(not(containsString("Exception")));
    }
}
