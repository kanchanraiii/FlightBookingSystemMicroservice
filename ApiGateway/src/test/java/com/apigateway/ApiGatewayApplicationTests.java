package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
	    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	    properties = {
	            "spring.cloud.config.enabled=false",
	            "spring.config.import=optional:classpath:/",
	            "eureka.client.enabled=false",
	            "spring.cloud.discovery.enabled=false",
	            "spring.main.web-application-type=reactive",
	            "spring.cloud.gateway.routes[0].id=dummy",
	            "spring.cloud.gateway.routes[0].uri=http://example.org",
	            "spring.cloud.gateway.routes[0].predicates[0]=Path=/dummy/**"
	    }
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
	                .expectStatus().isNotFound();
	    }

	    @Test
	    void bookingRouteMatched() {
	        webTestClient.post()
	                .uri("/booking/test")
	                .exchange()
	                .expectStatus().isNotFound();
	    }
	}
