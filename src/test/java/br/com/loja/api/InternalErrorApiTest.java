package br.com.loja.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import br.com.loja.service.CustomerService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InternalErrorApiTest {

    @LocalServerPort
    int port;

    @MockitoBean
    CustomerService service;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }

    @Test
    void internalErrorsDoNotLeakDetails() {
        when(service.findById(anyLong())).thenThrow(new RuntimeException("jdbc:postgresql://secret-host/db"));

        given().header("Accept-Language", "en-US").get("/api/v1/customers/1").then()
                .statusCode(500)
                .body("code", equalTo("INTERNAL_SERVER_ERROR"))
                .body("message", equalTo("Internal server error. Please contact support if the problem persists."))
                .body("success", equalTo(false))
                .body(not(containsString("secret")))
                .body(not(containsString("RuntimeException")))
                .body(not(containsString("at br.com")));
    }
}
