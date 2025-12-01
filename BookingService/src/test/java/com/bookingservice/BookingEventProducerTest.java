package com.bookingservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.bookingservice.model.BookingEvent;
import com.bookingservice.service.BookingEventProducer;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BookingEventProducerTest {

    @Mock
    KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @Test
    void publishesEvent() {
        CompletableFuture<SendResult<String, BookingEvent>> future =
                CompletableFuture.completedFuture(
                        new SendResult<>(
                                new org.apache.kafka.clients.producer.ProducerRecord<>("booking-events", new BookingEvent()),
                                new RecordMetadata(new TopicPartition("booking-events", 0), 0L, 0L, 0L, 0L, 0, 0)));

        when(kafkaTemplate.send(any(), any())).thenReturn(future);

        BookingEventProducer producer = new BookingEventProducer(kafkaTemplate, "booking-events");

        StepVerifier.create(producer.publish(new BookingEvent()))
                .verifyComplete();

        verify(kafkaTemplate, times(1)).send(any(), any());
    }
}
