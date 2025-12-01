package com.bookingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.bookingservice.model.BookingEvent;

import reactor.core.publisher.Mono;

@Component
public class BookingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private final String topic;

    public BookingEventProducer(
            KafkaTemplate<String, BookingEvent> kafkaTemplate,
            @Value("${booking.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public Mono<Void> publish(BookingEvent event) {
        if (event == null || topic == null || topic.isBlank()) {
            return Mono.empty();
        }
        return Mono.fromFuture(kafkaTemplate.send(topic, event))
                .doOnError(ex -> log.error("Failed to publish booking event", ex))
                .onErrorResume(ex -> Mono.empty())
                .then();
    }
}
