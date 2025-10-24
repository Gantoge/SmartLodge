package com.example.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = GatewaySmokeTests.WiremockInitializer.class)
public class GatewaySmokeTests {

    static class WiremockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        static WireMockServer wireMockServer = new WireMockServer(0);
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            wireMockServer.start();
            int port = wireMockServer.port();
            TestPropertyValues.of(
                    "spring.cloud.gateway.routes[0].id=mock-hotel",
                    "spring.cloud.gateway.routes[0].uri=http://localhost:" + port,
                    "spring.cloud.gateway.routes[0].predicates[0]=Path=/hotels/**",
                    "spring.cloud.gateway.routes[1].id=mock-booking",
                    "spring.cloud.gateway.routes[1].uri=http://localhost:" + port,
                    "spring.cloud.gateway.routes[1].predicates[0]=Path=/bookings/**"
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private WebClient.Builder builder;

    @BeforeEach
    void setup() {
        WiremockInitializer.wireMockServer.resetAll();
        WiremockInitializer.wireMockServer.stubFor(get(urlEqualTo("/hotels/test"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withHeader("X-Correlation-Id", matching(".*"))
                .willReturn(okJson("{\"ok\":true}")));
    }

    @AfterAll
    static void shutdown() {
        WiremockInitializer.wireMockServer.stop();
    }

    @Test
    void routesAndHeadersForwarded() {
        WebClient client = builder.baseUrl("http://localhost:8080").build();
        String body = client.get()
                .uri("/hotels/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .header("X-Correlation-Id", java.util.UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("ok"));
    }
}


