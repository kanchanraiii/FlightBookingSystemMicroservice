package com.flightservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flightservice.repository.AirlineRepository;
import com.flightservice.repository.FlightRepository;
import com.flightservice.repository.SeatsRepository;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=optional:classpath:/",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
class FlightServiceApplicationTests {

	@MockBean
	FlightRepository flightRepository;

	@MockBean
	AirlineRepository airlineRepository;

	@MockBean
	SeatsRepository seatsRepository;

	@Test
	void contextLoads() {
		// Smoke test: validates that the FlightService Spring context initializes.
	}

}
