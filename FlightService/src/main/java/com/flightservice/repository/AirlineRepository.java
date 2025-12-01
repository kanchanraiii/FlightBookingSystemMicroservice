package com.flightservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.flightservice.model.Airline;

public interface AirlineRepository extends ReactiveMongoRepository<Airline,String>{

}
