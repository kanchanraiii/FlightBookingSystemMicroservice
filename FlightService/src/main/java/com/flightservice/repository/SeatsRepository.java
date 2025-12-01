package com.flightservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightservice.model.Seats;

public interface SeatsRepository extends ReactiveMongoRepository<Seats,String>{

}
