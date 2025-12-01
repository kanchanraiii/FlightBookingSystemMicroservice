package com.flightservice.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightservice.exceptions.ResourceNotFoundException;
import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Flights;
import com.flightservice.repository.FlightRepository;
import com.flightservice.request.SearchFlightRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FlightSearchService {

    private final FlightRepository flightInventoryRepository;

    @Autowired
    public FlightSearchService(FlightRepository flightInventoryRepository) {
        this.flightInventoryRepository = flightInventoryRepository;
    }

    // to search a flight 
    public Flux<Flights> searchFlights(SearchFlightRequest req) {

        if (req.getSourceCity() == null) {
            return Flux.error(new ValidationException("Source city is required"));
        }

        if (req.getDestinationCity() == null) {
            return Flux.error(new ValidationException("Destination city is required"));
        }

        if (req.getSourceCity().equals(req.getDestinationCity())) {
            return Flux.error(new ValidationException("Source and destination cannot be the same"));
        }

        if (req.getTravelDate() == null) {
            return Flux.error(new ValidationException("Travel date is required"));
        }

        if (req.getTravelDate().isBefore(LocalDate.now())) {
            return Flux.error(new ValidationException("Travel date cannot be in the past"));
        }

        return flightInventoryRepository
                .findBySourceCityAndDestinationCityAndDepartureDate(
                        req.getSourceCity(),
                        req.getDestinationCity(),
                        req.getTravelDate()
                )
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("No flights found")));
    }
}
