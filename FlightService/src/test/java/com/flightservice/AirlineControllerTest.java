package com.flightservice;
import com.flightservice.model.Airline;
import com.flightservice.request.AddAirlineRequest;
import com.flightservice.service.AirlineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.flightservice.controller.MainController;


class AirlineControllerTests {

    private AirlineService airlineService;
    private MainController controller;

    @BeforeEach
    void setup() throws Exception {
        airlineService = Mockito.mock(AirlineService.class);
        controller = new MainController();
        var f = MainController.class.getDeclaredField("airlineService");
        f.setAccessible(true);
        f.set(controller, airlineService);
    }

    private Airline airline(String code) {
        Airline airline = new Airline();
        airline.setAirlineCode(code);
        airline.setAirlineName("Airline " + code);
        return airline;
    }

    @Test
    @DisplayName("Add airline returns created airline")
    void addAirline() {
        AddAirlineRequest req = new AddAirlineRequest();
        req.setAirlineCode("AI");
        req.setAirlineName("Air India");
        Mockito.when(airlineService.addAirline(Mockito.any(AddAirlineRequest.class)))
                .thenReturn(Mono.just(airline("AI")));

        StepVerifier.create(controller.addAirline(req))
                .expectNextMatches(a -> "AI".equals(a.getAirlineCode()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Get all airlines returns list")
    void getAllAirlines() {
        Mockito.when(airlineService.getAllAirlines())
                .thenReturn(Flux.just(airline("A1"), airline("A2")));

        StepVerifier.create(controller.getAllAirlines())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Get airline by code returns airline")
    void getAirline() {
        Mockito.when(airlineService.getAirline("AI")).thenReturn(Mono.just(airline("AI")));

        StepVerifier.create(controller.getAirline("AI"))
                .expectNextMatches(a -> "Airline AI".equals(a.getAirlineName()))
                .verifyComplete();
    }
}
