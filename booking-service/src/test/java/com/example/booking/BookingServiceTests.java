package com.example.booking;

import com.example.booking.model.Booking;
import com.example.booking.repo.BookingRepository;
import com.example.booking.service.BookingService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@ContextConfiguration(initializers = BookingServiceTests.WiremockInitializer.class)
public class BookingServiceTests {

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
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void resetWiremock() {
        WiremockInitializer.wireMockServer.resetAll();
    }

    @AfterAll
    static void shutdown() {
        WiremockInitializer.wireMockServer.stop();
    }

    @Test
    void successFlow_confirmed() {
        stubFor(post(urlPathMatching("/rooms/\\d+/hold")).willReturn(okJson("{}")));
        stubFor(post(urlPathMatching("/rooms/\\d+/confirm")).willReturn(okJson("{}")));

        Booking b = bookingService.createBooking(1L, 10L, LocalDate.now(), LocalDate.now().plusDays(1), "r1");
        Assertions.assertEquals(Booking.Status.CONFIRMED, b.getStatus());
    }

    @Test
    void failureFlow_cancelledWithCompensation() {
        stubFor(post(urlPathMatching("/rooms/\\d+/hold")).willReturn(serverError()));
        stubFor(post(urlPathMatching("/rooms/\\d+/release")).willReturn(okJson("{}")));

        Booking b = bookingService.createBooking(2L, 11L, LocalDate.now(), LocalDate.now().plusDays(1), "r2");
        Assertions.assertEquals(Booking.Status.CANCELLED, b.getStatus());
    }

    @Test
    void timeoutFlow_cancelled() {
        stubFor(post(urlPathMatching("/rooms/\\d+/hold")).willReturn(aResponse().withFixedDelay(2000).withStatus(200)));
        stubFor(post(urlPathMatching("/rooms/\\d+/release")).willReturn(okJson("{}")));

        Booking b = bookingService.createBooking(3L, 12L, LocalDate.now(), LocalDate.now().plusDays(1), "r3");
        Assertions.assertEquals(Booking.Status.CANCELLED, b.getStatus());
    }

    @Test
    void idempotency_noDuplicate() {
        stubFor(post(urlPathMatching("/rooms/\\d+/hold")).willReturn(okJson("{}")));
        stubFor(post(urlPathMatching("/rooms/\\d+/confirm")).willReturn(okJson("{}")));

        Booking b1 = bookingService.createBooking(4L, 13L, LocalDate.now(), LocalDate.now().plusDays(1), "r4");
        Booking b2 = bookingService.createBooking(4L, 13L, LocalDate.now(), LocalDate.now().plusDays(1), "r4");
        Assertions.assertEquals(b1.getId(), b2.getId());
    }

    @Test
    void suggestions_sorted() {
        WiremockInitializer.wireMockServer.stubFor(get(urlEqualTo("/hotels/rooms"))
                .willReturn(okJson("[{" +
                        "\"id\":1,\"number\":\"101\",\"timesBooked\":5},{" +
                        "\"id\":2,\"number\":\"102\",\"timesBooked\":1}]")));
        List<com.example.booking.service.BookingService.RoomView> res = bookingService.getRoomSuggestions().block();
        Assertions.assertEquals(2, res.size());
        Assertions.assertEquals(2L, res.get(0).id());
    }
}


