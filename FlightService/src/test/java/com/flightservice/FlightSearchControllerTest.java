package com.flightservice;

import com.flightservice.model.Cities;
import com.flightservice.model.Flights;
import com.flightservice.request.SearchFlightRequest;
import com.flightservice.service.FlightSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import com.flightservice.controller.*;
import java.time.LocalDate;
import java.time.LocalTime;
import com.flightservice.controller.MainController;

class FlightSearchControllerTests {

    private FlightSearchService searchService;
    private MainController controller;

    @BeforeEach
    void setup() throws Exception {
        searchService = Mockito.mock(FlightSearchService.class);
        controller = new MainController(null, null, searchService);
        var f = MainController.class.getDeclaredField("searchService");
        f.setAccessible(true);
        f.set(controller, searchService);
    }

    @Test
    @DisplayName("Search flights returns flight inventory")
    void searchFlights() {
        SearchFlightRequest req = new SearchFlightRequest();
        req.setSourceCity(Cities.KANPUR);
        req.setDestinationCity(Cities.MUMBAI);
        req.setTravelDate(LocalDate.now().plusDays(1));

        Flights inv = new Flights();
        inv.setFlightId("FL1");
        inv.setDepartureTime(LocalTime.NOON);
        Mockito.when(searchService.searchFlights(Mockito.any(SearchFlightRequest.class)))
                .thenReturn(Flux.just(inv));

        StepVerifier.create(controller.searchFlights(req))
                .expectNextMatches(f -> "FL1".equals(f.getFlightId()))
                .verifyComplete();
    }
}

