package com.flightservice.repository;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightservice.model.Cities;
import com.flightservice.model.Flights;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightRepository extends ReactiveMongoRepository<Flights,String> {

	Mono<Map<String, String>> findFirstByFlightNumberAndDepartureDate(String flightNumber, LocalDate departureDate);

	Flux<Flights> findBySourceCityAndDestinationCityAndDepartureDate(Cities sourceCity, Cities destinationCity,
			LocalDate travelDate);
}
