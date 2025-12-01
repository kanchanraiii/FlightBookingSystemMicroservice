package com.bookingservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.TripType;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;
import com.bookingservice.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MainController.class)
class MainControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookingService bookingService;

    @Test
    void bookFlight_createsBooking() {
        Booking booking = booking();
        when(bookingService.bookFlight(eq("FL-1"), any(BookingRequest.class)))
                .thenReturn(Mono.just(booking));

        BookingRequest request = new BookingRequest();
        request.setContactName("Jane");
        request.setContactEmail("jane@example.com");
        request.setTripType(TripType.ONE_WAY);
        PassengerRequest passenger = new PassengerRequest();
        passenger.setName("Alex");
        passenger.setAge(25);
        passenger.setGender(com.bookingservice.model.Gender.MALE);
        passenger.setSeatOutbound("1A");
        request.setPassengers(List.of(passenger));

        webTestClient.post()
                .uri("/api/booking/FL-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.bookingId").isEqualTo("b-1");
    }

    @Test
    void getTicket_returnsBooking() {
        Booking booking = booking();
        when(bookingService.getTicket("PNR001")).thenReturn(Mono.just(booking));

        webTestClient.get()
                .uri("/api/booking/ticket/PNR001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pnrOutbound").isEqualTo("PNR001");
    }

    @Test
    void getHistory_returnsFlux() {
        when(bookingService.getHistory("user@example.com"))
                .thenReturn(Flux.just(booking()));

        webTestClient.get()
                .uri("/api/booking/history/user@example.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].bookingId").isEqualTo("b-1");
    }

    @Test
    void cancelBooking_returnsMessage() {
        when(bookingService.cancelTicket("PNR001"))
                .thenReturn(Mono.just(Map.of("message", "cancelled")));

        webTestClient.delete()
                .uri("/api/booking/cancel/PNR001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("cancelled");
    }

    private Booking booking() {
        Booking booking = new Booking();
        booking.setBookingId("b-1");
        booking.setOutboundFlightId("FL-1");
        booking.setTripType(TripType.ONE_WAY);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPnrOutbound("PNR001");
        return booking;
    }
}