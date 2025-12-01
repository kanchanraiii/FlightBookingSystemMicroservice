package com.flightservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;

import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Airline;
import com.flightservice.model.Cities;
import com.flightservice.model.Flights;
import com.flightservice.repository.AirlineRepository;
import com.flightservice.repository.FlightRepository;
import com.flightservice.repository.SeatsRepository;
import com.flightservice.request.AddFlightRequest;
import com.flightservice.service.FlightService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FlightServiceValidationTest {

    @Mock
    FlightRepository flightRepository;

    @Mock
    AirlineRepository airlineRepository;

    @Mock
    SeatsRepository seatsRepository;

    @InjectMocks
    FlightService flightService;

    AddFlightRequest request;

    @BeforeEach
    void setup() {
        request = new AddFlightRequest();
        request.setAirlineCode("AI");
        request.setFlightNumber("AI101");
        request.setSourceCity(Cities.DELHI);
        request.setDestinationCity(Cities.MUMBAI);
        request.setDepartureDate(LocalDate.now().plusDays(1));
        request.setDepartureTime(LocalTime.of(10, 0));
        request.setArrivalDate(LocalDate.now().plusDays(1));
        request.setArrivalTime(LocalTime.of(12, 0));
        request.setTotalSeats(100);
        request.setPrice(5000f);

        when(airlineRepository.findById(anyString()))
                .thenReturn(Mono.just(new Airline()));

        when(flightRepository.findFirstByFlightNumberAndDepartureDate(any(), any()))
                .thenReturn(Mono.empty());

        lenient().when(flightRepository.save(any()))
                .thenReturn(Mono.just(new Flights()));

        lenient().when(seatsRepository.saveAll(anyIterable()))
                .thenReturn(Flux.empty());
    }

    @Test
    void flightNumberMissing() {
        request.setFlightNumber(null);

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void sourceCityMissing() {
        request.setSourceCity(null);

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void destinationCityMissing() {
        request.setDestinationCity(null);

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void sourceEqualsDestination() {
        request.setDestinationCity(Cities.DELHI);

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }


    @Test
    void totalSeatsInvalid() {
        request.setTotalSeats(0);

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void priceInvalid() {
        request.setPrice(-1f);

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void departureNotInFuture() {
        request.setDepartureDate(LocalDate.now().minusDays(1));

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void arrivalBeforeDeparture() {
        request.setArrivalTime(LocalTime.of(8, 0));

        StepVerifier.create(flightService.addInventory(request))
                .expectError(ValidationException.class)
                .verify();
    }
}
