package com.bookingservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import reactor.core.publisher.Mono;

@Component
public class FlightClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public FlightClient(WebClient.Builder builder,
                        CircuitBreakerRegistry registry) {

        this.webClient = builder
                .baseUrl("http://FLIGHT-SERVICE")
                .build();

        this.circuitBreaker =
                registry.circuitBreaker("flightServiceCB");
    }

    public Mono<FlightDto> getFlight(String flightId) {
        return webClient.get()
                .uri("/api/flight/getAllFlights")
                .retrieve()
                .bodyToFlux(FlightDto.class)
                .filter(f -> flightId.equals(f.getFlightId()))
                .next()
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(ex ->
                        Mono.error(new RuntimeException("FlightService unavailable")));
    }

    public Mono<Void> reserveSeats(String flightId, int seats) {
        return webClient.post()
                .uri("/api/flight/{id}/reserve?seats={s}", flightId, seats)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(ex -> Mono.error(
                        new RuntimeException("Seat reservation failed")));
    }

    public Mono<Void> releaseSeats(String flightId, int seats) {
        return webClient.post()
                .uri("/api/flight/{id}/release?seats={s}", flightId, seats)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(ex -> Mono.empty());
    }
}
