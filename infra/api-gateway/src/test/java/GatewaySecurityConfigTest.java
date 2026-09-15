import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.config.GatewaySecurityConfig;
import org.springframework.context.ApplicationContext;


@SpringJUnitConfig(classes = {GatewaySecurityConfig.class, GatewaySecurityTestConfig.class})
@TestPropertySource(properties = {
        "app.security.users[0].username=ivan",
        "app.security.users[0].password=ivan",
        "app.security.users[0].roles[0]=USER",

        "app.security.users[1].username=anna",
        "app.security.users[1].password=anna",
        "app.security.users[1].roles[0]=USER",
        "app.security.users[1].roles[1]=ADMIN"
})
class GatewaySecurityConfigTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .configureClient()
                .baseUrl("http://localhost")
                .build();
    }

    @Test
    void products_areAvailableWithoutAuthentication() {
        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getProduct_areAvailableWithoutAuthentication() {
        webTestClient.get()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrders_withoutAuthentication_isUnauthorized() {
        webTestClient.get()
                .uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getOrders_asUser_isForbidden() {
        webTestClient.get()
                .uri("/api/orders")
                .headers(httpHeaders ->  httpHeaders.setBasicAuth("ivan", "ivan"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getOrders_asAdmin_isAllowed() {
        webTestClient.get()
                .uri("/api/orders")
                .headers(httpHeaders ->  httpHeaders.setBasicAuth("anna", "anna"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void createOrder_withoutAuthentication_isUnauthorized() {
        webTestClient.post()
                .uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void createOrder_asUser_isAllowed() {
        webTestClient.post()
                .uri("/api/orders")
                .headers(httpHeaders -> httpHeaders.setBasicAuth("ivan", "ivan"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateProduct_asUser_isForbidden() {
        webTestClient.patch()
                .uri("/api/products/10")
                .headers(headers -> headers.setBasicAuth("ivan", "ivan"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateProduct_asAdmin_isAllowed() {
        webTestClient.patch()
                .uri("/api/products/10")
                .headers(headers -> headers.setBasicAuth("anna", "anna"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void createProduct_asUser_isForbidden() {
        webTestClient.post()
                .uri("/api/products")
                .headers(headers -> headers.setBasicAuth("ivan", "ivan"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createProduct_asAdmin_isAllowed() {
        webTestClient.post()
                .uri("/api/products")
                .headers(headers -> headers.setBasicAuth("anna", "anna"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getUnknown_asAdmin_isForbidden() {
        webTestClient.get()
                .uri("/api/unknown")
                .headers(headers -> headers.setBasicAuth("anna", "anna"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void preflight_isNotBlockedBySecurity() {
        webTestClient.options()
                .uri("/api/orders")
                .header("Origin", "http://localhost:8443")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                .exchange()
                .expectStatus().isOk();
    }
}