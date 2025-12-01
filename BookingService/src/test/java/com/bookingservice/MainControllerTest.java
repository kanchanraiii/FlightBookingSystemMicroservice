package com.bookingservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bookingservice.controller.MainController;
import com.bookingservice.exceptions.GlobalErrorHandler;
import com.bookingservice.model.Booking;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MainController.class)
@Import(GlobalErrorHandler.class)
class MainControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BookingService bookingService;

    @Test
    void bookFlightEndpoint() {
        Booking booking = new Booking();
        booking.setBookingId("B1");
        when(bookingService.bookFlight(Mockito.eq("F1"), any(BookingRequest.class)))
                .thenReturn(Mono.just(booking));

        webTestClient.post()
                .uri("/api/booking/F1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contactName": "Jane",
                          "contactEmail": "jane@example.com",
                          "tripType": "ONE_WAY",
                          "passengers": [
                            {"name":"A","age":25,"gender":"FEMALE","seatOutbound":"S1"}
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void getHistoryEndpoint() {
        when(bookingService.getHistory("a@b.com"))
                .thenReturn(Flux.just(new Booking()));

        webTestClient.get()
                .uri("/api/booking/history/a@b.com")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void cancelEndpoint() {
        when(bookingService.cancelTicket("PNR1"))
                .thenReturn(Mono.just(Map.of("message", "Booking cancelled")));

        webTestClient.delete()
                .uri("/api/booking/cancel/PNR1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Booking cancelled");
    }

    @Test
    void ticketEndpoint() {
        when(bookingService.getTicket("PNR1")).thenReturn(Mono.just(new Booking()));

        webTestClient.get()
                .uri("/api/booking/ticket/PNR1")
                .exchange()
                .expectStatus().isOk();
    }
}
