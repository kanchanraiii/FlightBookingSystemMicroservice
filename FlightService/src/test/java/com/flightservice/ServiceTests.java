package com.flightservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import com.flightservice.exceptions.ResourceNotFoundException;
import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Airline;
import com.flightservice.model.Cities;
import com.flightservice.model.Flights;
import com.flightservice.model.Seats;
import com.flightservice.repository.AirlineRepository;
import com.flightservice.repository.FlightRepository;
import com.flightservice.repository.SeatsRepository;
import com.flightservice.request.AddAirlineRequest;
import com.flightservice.request.AddFlightRequest;
import com.flightservice.service.AirlineService;
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
class ServiceLayerTest {

    @Mock
    AirlineRepository airlineRepository;

    @Mock
    FlightRepository flightRepository;

    @Mock
    SeatsRepository seatsRepository;

    @InjectMocks
    AirlineService airlineService;

    @InjectMocks
    FlightService flightService;

    AddAirlineRequest addAirlineRequest;
    AddFlightRequest addFlightRequest;

    @BeforeEach
    void setup() {
        addAirlineRequest = new AddAirlineRequest();
        addAirlineRequest.setAirlineCode("AI");
        addAirlineRequest.setAirlineName("Air India");

        addFlightRequest = new AddFlightRequest();
        addFlightRequest.setAirlineCode("AI");
        addFlightRequest.setFlightNumber("AI101");
        addFlightRequest.setSourceCity(Cities.DELHI);
        addFlightRequest.setDestinationCity(Cities.MUMBAI);
        addFlightRequest.setDepartureDate(LocalDate.now().plusDays(1));
        addFlightRequest.setDepartureTime(LocalTime.of(10, 0));
        addFlightRequest.setArrivalDate(LocalDate.now().plusDays(1));
        addFlightRequest.setArrivalTime(LocalTime.of(12, 0));
        addFlightRequest.setTotalSeats(2);
        addFlightRequest.setPrice(5000f);
        addFlightRequest.setMealAvailable(true);
    }


    @Test
    void addAirline_success() {
        when(airlineRepository.existsById("AI")).thenReturn(Mono.just(false));
        when(airlineRepository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(airlineService.addAirline(addAirlineRequest))
                .expectNextMatches(a -> a.getAirlineCode().equals("AI"))
                .verifyComplete();
    }

    @Test
    void addAirline_duplicate() {
        when(airlineRepository.existsById("AI")).thenReturn(Mono.just(true));

        StepVerifier.create(airlineService.addAirline(addAirlineRequest))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void addAirline_invalidCode() {
        addAirlineRequest.setAirlineCode("");

        StepVerifier.create(airlineService.addAirline(addAirlineRequest))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void getAllAirlines() {
        when(airlineRepository.findAll()).thenReturn(Flux.just(new Airline()));

        StepVerifier.create(airlineService.getAllAirlines())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAirline_notFound() {
        when(airlineRepository.findById("AI")).thenReturn(Mono.empty());

        StepVerifier.create(airlineService.getAirline("AI"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void addFlight_success() {
        Flights saved = new Flights();
        saved.setFlightId("F1");
        saved.setTotalSeats(2);

        when(airlineRepository.findById("AI")).thenReturn(Mono.just(new Airline()));
        when(flightRepository.findFirstByFlightNumberAndDepartureDate(any(), any()))
                .thenReturn(Mono.empty());
        when(flightRepository.save(any())).thenReturn(Mono.just(saved));
        when(seatsRepository.saveAll(anyList())).thenReturn(Flux.just(new Seats()));

        StepVerifier.create(flightService.addInventory(addFlightRequest))
                .expectNextMatches(m -> m.get("flightId").equals("F1"))
                .verifyComplete();
    }

    @Test
    void getAllFlights() {
        when(flightRepository.findAll()).thenReturn(Flux.just(new Flights()));

        StepVerifier.create(flightService.getAllFlights())
                .expectNextCount(1)
                .verifyComplete();
    }
}
