package com.bookingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.service.TicketService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    TicketService ticketService;

    @Test
    void rejectsEmptyPnr() {
        StepVerifier.create(ticketService.getTicketByPnr(" "))
                .verifyError(ValidationException.class);
    }

    @Test
    void rejectsInvalidLength() {
        StepVerifier.create(ticketService.getTicketByPnr("ABC"))
                .verifyError(ValidationException.class);
    }

    @Test
    void rejectsNonAlphanumeric() {
        StepVerifier.create(ticketService.getTicketByPnr("ABC-12"))
                .verifyError(ValidationException.class);
    }

    @Test
    void returnsBookingWhenFound() {
        Booking booking = new Booking();
        booking.setPnrOutbound("ABC123");
        when(bookingRepository.findByPnrOutbound("ABC123")).thenReturn(Mono.just(booking));
        when(bookingRepository.findByPnrReturn("ABC123")).thenReturn(Mono.empty());

        StepVerifier.create(ticketService.getTicketByPnr("ABC123"))
                .assertNext(b -> {
                    assertNotNull(b);
                    assertEquals("ABC123", b.getPnrOutbound());
                })
                .verifyComplete();
    }

    @Test
    void fallsBackToReturnPnr() {
        Booking booking = new Booking();
        booking.setPnrReturn("RET123");
        when(bookingRepository.findByPnrOutbound(anyString())).thenReturn(Mono.empty());
        when(bookingRepository.findByPnrReturn("RET123")).thenReturn(Mono.just(booking));

        StepVerifier.create(ticketService.getTicketByPnr("RET123"))
                .assertNext(b -> assertEquals("RET123", b.getPnrReturn()))
                .verifyComplete();
    }

    @Test
    void notFoundAnywhereThrows() {
        when(bookingRepository.findByPnrOutbound("ABC123")).thenReturn(Mono.empty());
        when(bookingRepository.findByPnrReturn("ABC123")).thenReturn(Mono.empty());

        StepVerifier.create(ticketService.getTicketByPnr("ABC123"))
                .verifyError(ResourceNotFoundException.class);
    }
}
