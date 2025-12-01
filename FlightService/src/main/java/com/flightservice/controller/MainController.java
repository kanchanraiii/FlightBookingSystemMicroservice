package com.flightservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import com.flightservice.model.Flights;
import com.flightservice.request.SearchFlightRequest;
import com.flightservice.request.AddFlightRequest;
import com.flightservice.service.FlightService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.flightservice.service.AirlineService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.flightservice.model.Airline;
import com.flightservice.request.AddAirlineRequest;
import com.flightservice.service.FlightSearchService;

@RestController
@RequestMapping("/api/flight")
public class MainController {
	
	 private final AirlineService airlineService;
	 
	 private final FlightService flightService;
	 
	 private final FlightSearchService searchService;

	 @Autowired
	 public MainController(
			 AirlineService airlineService,
			 FlightService flightService,
			 FlightSearchService searchService) {
		 this.airlineService = airlineService;
		 this.flightService = flightService;
		 this.searchService = searchService;
	 }

	    // to add an airline
	    @PostMapping("/addAirline")
	    @ResponseStatus(HttpStatus.CREATED)
	    public Mono<Airline> addAirline(@RequestBody AddAirlineRequest req) {
	        return airlineService.addAirline(req);
	    }

	    // to get airlines in db
	    @GetMapping("/getAllAirlines")
	    @ResponseStatus(HttpStatus.OK)
	    public Flux<Airline> getAllAirlines() {
	        return airlineService.getAllAirlines();
	    }

	    // to get airline with its code
	    @GetMapping("/getAirline/{code}")
	    @ResponseStatus(HttpStatus.OK)
	    public Mono<Airline> getAirline(@PathVariable String code) {
	        return airlineService.getAirline(code);
	    }
	    
	    // to add a flight
		@PostMapping("/airline/inventory/add")
		@ResponseStatus(HttpStatus.CREATED)
		public Mono<Map<String, String>> addFlight(@RequestBody AddFlightRequest request) {
		    return flightService.addInventory(request);
		}

	    // to search a flight
	    @PostMapping("/search")
	    @ResponseStatus(HttpStatus.OK)
	    public Flux<Flights> searchFlights(@Valid @RequestBody SearchFlightRequest req) {
	        return searchService.searchFlights(req);
	    }
	    
	    //to get all flights
	    @GetMapping("/getAllFlights")
	    @ResponseStatus(HttpStatus.OK)
	    public Flux<Flights>getAllFlights(){
	    	return flightService.getAllFlights();
	    }
	

}
