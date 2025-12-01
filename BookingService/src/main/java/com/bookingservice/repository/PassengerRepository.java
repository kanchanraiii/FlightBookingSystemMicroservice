package com.bookingservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.bookingservice.model.Passenger;

public interface PassengerRepository extends ReactiveMongoRepository<Passenger, String>{

}
