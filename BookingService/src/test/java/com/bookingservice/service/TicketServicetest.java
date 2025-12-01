package com.bookingservice.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.TripType;
import com.bookingservice.repository.BookingRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void getTicketByPnr_rejectsBlank() {
        StepVerifier.create(ticketService.getTicketByPnr("  "))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("PNR cannot be empty", error.getMessage());
                })
                .verify();
    }

    @Test
    void getTicketByPnr_rejectsWrongLength() {
        StepVerifier.create(ticketService.getTicketByPnr("ABC"))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("PNR must be exactly 6 characters", error.getMessage());
                })
                .verify();
    }

    @Test
    void getTicketByPnr_rejectsNonAlphanumeric() {
        StepVerifier.create(ticketService.getTicketByPnr("ABC$12"))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("PNR must be alphanumeric", error.getMessage());
                })
                .verify();
    }

    @Test
    void getTicketByPnr_returnsOutboundBooking() {
        Booking booking = booking("ABC123");
        when(bookingRepository.findByPnrOutbound("ABC123")).thenReturn(Mono.just(booking));
        when(bookingRepository.findByPnrReturn("ABC123")).thenReturn(Mono.empty());

        StepVerifier.create(ticketService.getTicketByPnr("ABC123"))
                .expectNext(booking)
                .verifyComplete();
    }

    @Test
    void getTicketByPnr_returnsReturnBookingWhenOutboundEmpty() {
        Booking booking = booking("XYZ999");
        when(bookingRepository.findByPnrOutbound("XYZ999")).thenReturn(Mono.empty());
        when(bookingRepository.findByPnrReturn("XYZ999")).thenReturn(Mono.just(booking));

        StepVerifier.create(ticketService.getTicketByPnr("XYZ999"))
                .expectNext(booking)
                .verifyComplete();
    }

    @Test
    void getTicketByPnr_notFoundRaisesResourceNotFound() {
        when(bookingRepository.findByPnrOutbound("MISS12")).thenReturn(Mono.empty());
        when(bookingRepository.findByPnrReturn("MISS12")).thenReturn(Mono.empty());

        StepVerifier.create(ticketService.getTicketByPnr("MISS12"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    private Booking booking(String pnr) {
        Booking booking = new Booking();
        booking.setBookingId("b-1");
        booking.setTripType(TripType.ONE_WAY);
        booking.setOutboundFlightId("FL-1");
        booking.setPnrOutbound(pnr);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPassengers(1);
        booking.setContactEmail("test@example.com");
        booking.setContactName("Test User");
        return booking;
    }
}