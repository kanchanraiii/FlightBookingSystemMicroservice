package com.flightservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flightservice.exceptions.ResourceNotFoundException;
import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Flights;
import com.flightservice.model.Seats;
import com.flightservice.repository.AirlineRepository;
import com.flightservice.repository.FlightRepository;
import com.flightservice.repository.SeatsRepository;
import com.flightservice.request.AddFlightRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FlightService {

    private final FlightRepository flightInventoryRepository;

    private final AirlineRepository airlineRepository;

    private final SeatsRepository seatRepository;

    @Autowired
    public FlightService(
            FlightRepository flightInventoryRepository,
            AirlineRepository airlineRepository,
            SeatsRepository seatRepository) {
        this.flightInventoryRepository = flightInventoryRepository;
        this.airlineRepository = airlineRepository;
        this.seatRepository = seatRepository;
    }

    // add a flight in db
    public Mono<Map<String, String>> addInventory(AddFlightRequest req) {

        return validateRequest(req)
                .then(validateDates(req))
                .then(
                    airlineRepository.findById(req.getAirlineCode())
                        .switchIfEmpty(Mono.error(
                            new ResourceNotFoundException("Airline not found")
                        ))
                )
                .then(checkDuplicateFlight(req))
                .switchIfEmpty(createInventory(req))     
                .map(saved ->
                    Map.of("flightId", saved.getFlightId())
                );
    }

    // validation logic for adding flights
    private Mono<Void> validateRequest(AddFlightRequest req) {
        if (req.getFlightNumber() == null || req.getFlightNumber().isBlank()) {
            return Mono.error(new ValidationException("Flight number is required"));
        }
        if (req.getSourceCity() == null) {
            return Mono.error(new ValidationException("Source city is required"));
        }
        if (req.getDestinationCity() == null) {
            return Mono.error(new ValidationException("Destination city is required"));
        }
        if (req.getSourceCity().equals(req.getDestinationCity())) {
            return Mono.error(new ValidationException("Source and destination cannot be the same"));
        }
        if (req.getDepartureDate() == null || req.getDepartureTime() == null) {
            return Mono.error(new ValidationException("Departure date & time are required"));
        }
        if (req.getArrivalDate() == null || req.getArrivalTime() == null) {
            return Mono.error(new ValidationException("Arrival date & time are required"));
        }
        if (req.getTotalSeats() == null || req.getTotalSeats() <= 0) {
            return Mono.error(new ValidationException("Total seats must be greater than 0"));
        }
        if (req.getPrice() == null || req.getPrice() <= 0) {
            return Mono.error(new ValidationException("Price must be greater than 0"));
        }
        return Mono.empty();
    }

    
    // validation logic for dates
    private Mono<Void> validateDates(AddFlightRequest req) {
        LocalDateTime departure = LocalDateTime.of(req.getDepartureDate(), req.getDepartureTime());
        LocalDateTime arrival = LocalDateTime.of(req.getArrivalDate(), req.getArrivalTime());

        if (!departure.isAfter(LocalDateTime.now())) {
            return Mono.error(new ValidationException("Departure must be in the future"));
        }
        if (!arrival.isAfter(departure)) {
            return Mono.error(new ValidationException("Arrival must be after departure"));
        }
        return Mono.empty();
    }

    
    // to check if a duplicate flight exists or not
    private Mono<Flights> checkDuplicateFlight(AddFlightRequest req) {
        return flightInventoryRepository
                .findFirstByFlightNumberAndDepartureDate(req.getFlightNumber(), req.getDepartureDate())
                .flatMap(existing -> Mono.error(
                        new ValidationException("Flight already exists on this date")));
    }

    

    private Mono<Flights> createInventory(AddFlightRequest req) {
        Flights inv = mapToEntity(req);

        return flightInventoryRepository.save(inv)
                .flatMap(saved -> generateSeats(saved.getFlightId(), saved.getTotalSeats())
                        .thenReturn(saved));
    }

    
    private Flights mapToEntity(AddFlightRequest req) {
        Flights inv = new Flights();
        inv.setAirlineCode(req.getAirlineCode());
        inv.setFlightNumber(req.getFlightNumber());
        inv.setSourceCity(req.getSourceCity());
        inv.setDestinationCity(req.getDestinationCity());
        inv.setDepartureDate(req.getDepartureDate());
        inv.setDepartureTime(req.getDepartureTime());
        inv.setArrivalDate(req.getArrivalDate());
        inv.setArrivalTime(req.getArrivalTime());
        inv.setMealAvailable(req.isMealAvailable());
        inv.setTotalSeats(req.getTotalSeats());
        inv.setAvailableSeats(req.getTotalSeats());
        inv.setPrice(req.getPrice());
        return inv;
    }

    // to generate seats
    private Mono<Void> generateSeats(String flightId, int totalSeats) {
        List<Seats> seats = new ArrayList<>();

        for (int i = 1; i <= totalSeats; i++) {
            Seats s = new Seats();
            s.setFlightId(flightId);
            s.setSeatNo("S" + i);
            s.setBooked(false);
            seats.add(s);
        }
        return seatRepository.saveAll(seats).then();
    }

	// to get all flights
    public Flux<Flights> getAllFlights() {
		return flightInventoryRepository.findAll();
	}
}
