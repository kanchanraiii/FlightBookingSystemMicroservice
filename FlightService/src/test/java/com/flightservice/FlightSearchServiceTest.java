package com.flightservice;

import com.flightservice.exceptions.ResourceNotFoundException;
import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Cities;
import com.flightservice.model.Flights;
import com.flightservice.repository.FlightRepository;
import com.flightservice.request.SearchFlightRequest;
import com.flightservice.service.FlightSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightSearchServiceTests {

    @Mock
    private FlightRepository inventoryRepository;

    @InjectMocks
    private FlightSearchService flightSearchService;

    private SearchFlightRequest request(LocalDate travelDate) {
        SearchFlightRequest req = new SearchFlightRequest();
        req.setSourceCity(Cities.KANPUR);
        req.setDestinationCity(Cities.MUMBAI);
        req.setTravelDate(travelDate);
        return req;
    }

    private Flights sampleFlight(LocalDate departureDate) {
        Flights inv = new Flights();
        inv.setFlightId("FL-201");
        inv.setFlightNumber("AI201");
        inv.setSourceCity(Cities.KANPUR);
        inv.setDestinationCity(Cities.MUMBAI);
        inv.setDepartureDate(departureDate);
        inv.setDepartureTime(LocalTime.of(9, 0));
        inv.setPrice(4000.0);
        return inv;
    }

    @Test
    @DisplayName("Search flight with valid source, destination and date")
    void searchOneWayFlightWithValidDetails() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        SearchFlightRequest req = request(futureDate);

        when(inventoryRepository.findBySourceCityAndDestinationCityAndDepartureDate(
                req.getSourceCity(), req.getDestinationCity(), req.getTravelDate()))
                .thenReturn(Flux.just(sampleFlight(futureDate)));

        StepVerifier.create(flightSearchService.searchFlights(req))
                .assertNext(f -> assertEquals(futureDate, f.getDepartureDate()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Search results contain key fields")
    void searchResultsContainKeyFields() {
        LocalDate futureDate = LocalDate.now().plusDays(7);
        SearchFlightRequest req = request(futureDate);

        when(inventoryRepository.findBySourceCityAndDestinationCityAndDepartureDate(
                any(), any(), any())).thenReturn(Flux.just(sampleFlight(futureDate)));

        StepVerifier.create(flightSearchService.searchFlights(req))
                .assertNext(result -> {
                    assertNotNull(result.getDepartureDate());
                    assertNotNull(result.getDepartureTime());
                    assertNotNull(result.getPrice());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Error when no flights exist for route")
    void noFlightsForRouteThrowsNotFound() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        SearchFlightRequest req = request(futureDate);

        when(inventoryRepository.findBySourceCityAndDestinationCityAndDepartureDate(
                req.getSourceCity(), req.getDestinationCity(), req.getTravelDate()))
                .thenReturn(Flux.empty());

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Reject when source and destination are the same")
    void rejectSameSourceDestination() {
    	SearchFlightRequest req = request(LocalDate.now().plusDays(2));
        req.setDestinationCity(req.getSourceCity());

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ValidationException.class);
    }

    @Test
    @DisplayName("Reject when destination city is missing")
    void rejectMissingDestination() {
    	SearchFlightRequest req = request(LocalDate.now().plusDays(2));
        req.setDestinationCity(null);

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ValidationException.class);
    }

    @Test
    @DisplayName("Reject when travel date is missing")
    void rejectMissingTravelDate() {
    	SearchFlightRequest req = request(null);

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ValidationException.class);
    }

    @Test
    @DisplayName("Reject when travel date is in the past")
    void rejectPastDate() {
    	SearchFlightRequest req = request(LocalDate.now().minusDays(1));

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ValidationException.class);
    }

    @Test
    @DisplayName("Reject when required fields are missing")
    void rejectMissingFields() {
    	SearchFlightRequest req = new SearchFlightRequest();
        req.setDestinationCity(Cities.MUMBAI);
        req.setTravelDate(LocalDate.now().plusDays(1));

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ValidationException.class);

        req = request(LocalDate.now().plusDays(1));
        req.setSourceCity(null);

        StepVerifier.create(flightSearchService.searchFlights(req))
                .verifyError(ValidationException.class);
    }
}

