package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
	    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
	)
	class GatewayRouteTest {

	    @Autowired
	    WebTestClient webTestClient;

	    @Test
	    void contextLoads() {
	        // Smoke test: fails if the Spring context or routes cannot start.
	    }

	    @Test
	    void flightRouteMatched() {
	        webTestClient.get()
	                .uri("/flight/test")
	                .exchange()
	                .expectStatus().is5xxServerError();
	    }

	    @Test
	    void bookingRouteMatched() {
	        webTestClient.post()
	                .uri("/booking/test")
	                .exchange()
	                .expectStatus().is5xxServerError();
	    }
	}
