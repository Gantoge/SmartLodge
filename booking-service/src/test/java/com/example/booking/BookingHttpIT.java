package com.example.booking;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = BookingHttpIT.WiremockInitializer.class)
public class BookingHttpIT {

    static class WiremockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        static WireMockServer wireMockServer = new WireMockServer(0);
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            wireMockServer.start();
            int port = wireMockServer.port();
            TestPropertyValues.of(
                    "hotel.base-url=http://localhost:" + port,
                    "hotel.timeout-ms=1000",
                    "hotel.retries=1"
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    private String tokenUser() {
        byte[] bytes = "dev-secret-please-change".getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("100")
                .addClaims(Map.of("scope", "USER", "username", "it-user"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(bytes))
                .compact();
    }

    @BeforeEach
    void setupWiremock() {
        WiremockInitializer.wireMockServer.resetAll();
    }

    @AfterAll
    static void shutdown() {
        WiremockInitializer.wireMockServer.stop();
    }

    @Test
    void createBooking_Http_Success() {
        WiremockInitializer.wireMockServer.stubFor(post(urlPathMatching("/rooms/\\d+/hold")).willReturn(okJson("{}")));
        WiremockInitializer.wireMockServer.stubFor(post(urlPathMatching("/rooms/\\d+/confirm")).willReturn(okJson("{}")));

        webTestClient.post().uri("/bookings")
                .header("Authorization", "Bearer " + tokenUser())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{" +
                        "\"roomId\":1," +
                        "\"startDate\":\"2025-10-20\"," +
                        "\"endDate\":\"2025-10-22\"," +
                        "\"requestId\":\"" + UUID.randomUUID() + "\"}")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().jsonPath("$.status").isEqualTo("CONFIRMED");
    }
}


