package com.bookingservice.repository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.bookingservice.model.Booking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking,String>{

	Mono<Booking> findByPnrOutbound(String pnr);
	Mono<Booking> findByPnrReturn(String pnr);
	Flux<Booking> findByContactEmail(String email);
}
