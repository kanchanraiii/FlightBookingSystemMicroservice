package com.bookingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.bookingservice.client.FlightClient;
import com.bookingservice.client.FlightDto;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FlightClientTest {

    private FlightClient client;

    @BeforeEach
    void setup() {
        ExchangeFunction exchangeFunction = this::mockExchange;
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        client = new FlightClient(builder, CircuitBreakerRegistry.ofDefaults());
    }

    private Mono<ClientResponse> mockExchange(ClientRequest request) {
        URI uri = request.url();
        String path = uri.getPath();

        if (path.contains("/getAllFlights")) {
            List<Map<String, Object>> flights = List.of(
                    Map.of("flightId", "F1", "availableSeats", 3),
                    Map.of("flightId", "F2", "availableSeats", 0));
            String json;
            try {
                json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(flights);
            } catch (Exception e) {
                json = "[]";
            }
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build());
        }

        if (path.contains("/reserve") || path.contains("/release")) {
            // simulate success
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .body("")
                    .build());
        }

        // default 500 to trigger onErrorResume in client
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("")
                .build());
    }

    @Test
    void getFlightReturnsMatch() {
        StepVerifier.create(client.getFlight("F1"))
                .assertNext(dto -> assertEquals("F1", dto.getFlightId()))
                .verifyComplete();
    }

    @Test
    void getFlightEmptyWhenNotFound() {
        StepVerifier.create(client.getFlight("UNKNOWN"))
                .verifyComplete();
    }

    @Test
    void reserveSeatsCompletes() {
        StepVerifier.create(client.reserveSeats("F1", 2))
                .verifyComplete();
    }

    @Test
    void releaseSeatsCompletesOnError() {
        // force error path by using unmatched path to hit default 500
        ExchangeFunction errorExchange = req -> Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
        FlightClient errorClient = new FlightClient(WebClient.builder().exchangeFunction(errorExchange),
                CircuitBreakerRegistry.ofDefaults());

        StepVerifier.create(errorClient.releaseSeats("F_ERR", 1))
                .verifyComplete();
    }

    @Test
    void getFlightReturnsUnavailableOnError() {
        ExchangeFunction errorExchange = req -> Mono.error(new RuntimeException("down"));
        FlightClient errorClient = new FlightClient(WebClient.builder().exchangeFunction(errorExchange),
                CircuitBreakerRegistry.ofDefaults());

        StepVerifier.create(errorClient.getFlight("ANY"))
                .verifyErrorMessage("FlightService unavailable");
    }

    @Test
    void reserveSeatsPropagatesFailure() {
        ExchangeFunction errorExchange = req -> Mono.error(new RuntimeException("down"));
        FlightClient errorClient = new FlightClient(WebClient.builder().exchangeFunction(errorExchange),
                CircuitBreakerRegistry.ofDefaults());

        StepVerifier.create(errorClient.reserveSeats("F1", 1))
                .verifyErrorMessage("Seat reservation failed");
    }
}
