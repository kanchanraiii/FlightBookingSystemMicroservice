package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
	    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
	)
	class GatewayRouteTest {

	    @Autowired
	    WebTestClient webTestClient;

	    @Test
	    void contextLoads() {
	        assertNotNull(webTestClient);
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
