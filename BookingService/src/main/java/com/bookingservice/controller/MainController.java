package com.bookingservice.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.bookingservice.model.Booking;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/booking")
public class MainController {

	@Autowired
	private BookingService bookingService;

	// Book flight
	@PostMapping("/{flightId}")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Booking> bookFlight(@PathVariable String flightId, @Valid @RequestBody BookingRequest req) {

		return bookingService.bookFlight(flightId, req);
	}

	// Get ticket by PNR
	@GetMapping("/ticket/{pnr}")
	@ResponseStatus(HttpStatus.OK)
	public Mono<Booking> getTicket(@PathVariable String pnr) {
		return bookingService.getTicket(pnr);
	}

	// Get booking history by email
	@GetMapping("/history/{email}")
	@ResponseStatus(HttpStatus.OK)
	public Flux<Booking> getHistory(@PathVariable String email) {
		return bookingService.getHistory(email);
	}

	// Cancel booking
	@DeleteMapping("/cancel/{pnr}")
	public Mono<Map<String, String>> cancel(@PathVariable String pnr) {
		return bookingService.cancelTicket(pnr);
	}
}
