package com.bookingservice.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;  

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FlightClientTest {

    private final CircuitBreakerRegistry registry =
            CircuitBreakerRegistry.ofDefaults();  

    private final FlightClient flightClient = new FlightClient(
            WebClient.builder().exchangeFunction(new StubExchangeFunction()),
            registry                                           
    );

    @Test
    void getFlight_returnsFilteredFlight() {
        StepVerifier.create(flightClient.getFlight("MATCH"))
                .assertNext(dto -> {
                    assertEquals("MATCH", dto.getFlightId());
                    assertEquals(5, dto.getAvailableSeats());
                })
                .verifyComplete();
    }

    @Test
    void reserveAndReleaseSeats_completeSuccessfully() {
        StepVerifier.create(flightClient.reserveSeats("MATCH", 2))
                .verifyComplete();

        StepVerifier.create(flightClient.releaseSeats("MATCH", 1))
                .verifyComplete();
    }

    private static class StubExchangeFunction implements ExchangeFunction {
        @Override
        public Mono<ClientResponse> exchange(ClientRequest request) {
            if (request.method() == HttpMethod.GET) {
                String json = """
                        [
                          {"flightId":"MATCH","availableSeats":5},
                          {"flightId":"OTHER","availableSeats":2}
                        ]
                        """;
                var buffer = new DefaultDataBufferFactory()
                        .wrap(json.getBytes(StandardCharsets.UTF_8));
                ClientResponse response = ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(Flux.just(buffer))
                        .build();
                return Mono.just(response);
            }

            ClientResponse response = ClientResponse.create(HttpStatus.OK).build();
            return Mono.just(response);
        }
    }
}
