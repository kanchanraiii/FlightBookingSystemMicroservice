package com.flightservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightservice.exceptions.ResourceNotFoundException;
import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Airline;
import com.flightservice.repository.AirlineRepository;
import com.flightservice.request.AddAirlineRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AirlineService {
    
    private final AirlineRepository airlineRepository;

    @Autowired
    public AirlineService(AirlineRepository airlineRepository) {
        this.airlineRepository = airlineRepository;
    }

    // add an airline
    public Mono<Airline> addAirline(AddAirlineRequest req) {

        if (req.getAirlineCode() == null || req.getAirlineCode().isBlank()) {
            return Mono.error(new ValidationException("Airline code is required"));
        }

        if (req.getAirlineCode().length() > 5) {
            return Mono.error(new ValidationException("Airline code must not exceed 5 characters"));
        }

        if (req.getAirlineName() == null || req.getAirlineName().isBlank()) {
            return Mono.error(new ValidationException("Airline name is required"));
        }

        String code = req.getAirlineCode().toUpperCase();

        return airlineRepository.existsById(code)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ValidationException(
                                "Airline with code " + code + " already exists"));
                    }

                    Airline airline = new Airline();
                    airline.setAirlineCode(code);
                    airline.setAirlineName(req.getAirlineName());

                    return airlineRepository.save(airline);
                });

    }

    // fetch all airlines
    public Flux<Airline> getAllAirlines() {
        return airlineRepository.findAll();
    }

    // fetch airline by code
    public Mono<Airline> getAirline(String code) {
        return airlineRepository.findById(code.toUpperCase())
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Airline not found with code: " + code)));
    }
}
