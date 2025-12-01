package com.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.bookingservice.client.FlightClient;
import com.bookingservice.client.FlightDto;
import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.TripType;
import com.bookingservice.service.BookingEventProducer;
import com.bookingservice.service.EmailService;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PassengerRepository;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private FlightClient flightClient;

    @Mock
    private BookingEventProducer eventProducer;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    private AtomicInteger bookingIdSequence;

    @BeforeEach
    void setUp() {
        bookingIdSequence = new AtomicInteger();
        when(passengerRepository.saveAll(any(Flux.class))).thenReturn(Flux.empty());
        when(eventProducer.publish(any())).thenReturn(Mono.empty());
        when(emailService.sendBookingNotification(any(), any())).thenReturn(Mono.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking toSave = invocation.getArgument(0);
            if (toSave.getBookingId() == null) {
                toSave.setBookingId("booking-" + bookingIdSequence.incrementAndGet());
            }
            return Mono.just(toSave);
        });
    }

    @Test
    void bookFlight_oneWay_reservesSeatsAndPersistsBooking() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        FlightDto outbound = flight("OUT-1", 5);

        when(flightClient.getFlight("OUT-1")).thenReturn(Mono.just(outbound));
        when(flightClient.reserveSeats("OUT-1", request.getPassengers().size()))
                .thenReturn(Mono.empty());

        StepVerifier.create(bookingService.bookFlight("OUT-1", request))
                .assertNext(booking -> {
                    assertEquals("OUT-1", booking.getOutboundFlightId());
                    assertNull(booking.getReturnFlight());
                    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
                    assertEquals(TripType.ONE_WAY, booking.getTripType());
                    assertEquals(request.getPassengers().size(), booking.getTotalPassengers());
                    assertNotNull(booking.getPnrOutbound());
                })
                .verifyComplete();

        verify(flightClient, times(1))
                .reserveSeats("OUT-1", request.getPassengers().size());
        verify(passengerRepository, times(1)).saveAll(any(Flux.class));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void bookFlight_roundTrip_reservesBothLegs() {
        BookingRequest request = buildRequest(TripType.ROUND_TRIP, true);
        FlightDto outbound = flight("OUT-2", 4);
        FlightDto inbound = flight("RET-2", 4);

        when(flightClient.getFlight("OUT-2")).thenReturn(Mono.just(outbound));
        when(flightClient.getFlight("RET-2")).thenReturn(Mono.just(inbound));
        when(flightClient.reserveSeats(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.bookFlight("OUT-2", request))
                .assertNext(booking -> {
                    assertEquals("OUT-2", booking.getOutboundFlightId());
                    assertEquals("RET-2", booking.getReturnFlight());
                    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
                    assertEquals(TripType.ROUND_TRIP, booking.getTripType());
                })
                .verifyComplete();

        verify(flightClient, times(1))
                .reserveSeats("OUT-2", request.getPassengers().size());
        verify(flightClient, times(1))
                .reserveSeats("RET-2", request.getPassengers().size());
        verify(passengerRepository, times(1)).saveAll(any(Flux.class));
    }

    @Test
    void bookFlight_notEnoughSeats_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        FlightDto outbound = flight("OUT-3", 1); // fewer seats than passengers

        when(flightClient.getFlight("OUT-3")).thenReturn(Mono.just(outbound));

        StepVerifier.create(bookingService.bookFlight("OUT-3", request))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Not enough seats", error.getMessage());
                })
                .verify();

        verify(bookingRepository, never()).save(any());
        verify(flightClient, never()).reserveSeats(anyString(), anyInt());
        verify(passengerRepository, never()).saveAll(any(Flux.class));
    }

    @Test
    void bookFlight_roundTripWithoutReturnFlight_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ROUND_TRIP, false);

        assertThrows(ValidationException.class,
                () -> bookingService.bookFlight("OUT-4", request));

        verify(flightClient, never()).getFlight(anyString());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookFlight_passengerListMissing_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        request.setPassengers(null);

        assertThrows(ValidationException.class,
                () -> bookingService.bookFlight("OUT-10", request));

        verify(flightClient, never()).getFlight(anyString());
    }

    @Test
    void bookFlight_tripTypeMissing_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        request.setTripType(null);

        assertThrows(ValidationException.class,
                () -> bookingService.bookFlight("OUT-11", request));
    }

    @Test
    void bookFlight_roundTripMissingReturnSeat_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ROUND_TRIP, true);
        request.getPassengers().forEach(p -> p.setSeatReturn(null));
        request.setReturnFlightId("RET-12");

        when(flightClient.getFlight("OUT-12")).thenReturn(Mono.just(flight("OUT-12", 5)));
        when(flightClient.getFlight("RET-12")).thenReturn(Mono.just(flight("RET-12", 5)));

        StepVerifier.create(bookingService.bookFlight("OUT-12", request))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Return seat required", error.getMessage());
                })
                .verify();
    }

    @Test
    void bookFlight_roundTripReturnSeatsInsufficient_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ROUND_TRIP, true);
        request.setReturnFlightId("RET-13");

        when(flightClient.getFlight("OUT-13")).thenReturn(Mono.just(flight("OUT-13", 5)));
        when(flightClient.getFlight("RET-13")).thenReturn(Mono.just(flight("RET-13", 1)));

        StepVerifier.create(bookingService.bookFlight("OUT-13", request))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Not enough seats", error.getMessage());
                })
                .verify();
    }

    @Test
    void bookFlight_invalidPassengerAge_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        request.getPassengers().get(0).setAge(0);

        when(flightClient.getFlight("OUT-14")).thenReturn(Mono.just(flight("OUT-14", 5)));

        StepVerifier.create(bookingService.bookFlight("OUT-14", request))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Invalid age", error.getMessage());
                })
                .verify();

        verify(flightClient, times(1)).getFlight("OUT-14");
        verify(flightClient, never()).reserveSeats(anyString(), anyInt());
    }

    @Test
    void bookFlight_missingOutboundSeat_throwsValidationException() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        request.getPassengers().forEach(p -> p.setSeatOutbound(null));

        when(flightClient.getFlight("OUT-15")).thenReturn(Mono.just(flight("OUT-15", 5)));

        StepVerifier.create(bookingService.bookFlight("OUT-15", request))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Outbound seat required", error.getMessage());
                })
                .verify();
    }

    @Test
    void bookFlight_outboundNotFound_throwsResourceNotFound() {
        BookingRequest request = buildRequest(TripType.ONE_WAY, false);
        when(flightClient.getFlight("OUT-16")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.bookFlight("OUT-16", request))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void bookFlight_roundTripReturnNotFound_throwsResourceNotFound() {
        BookingRequest request = buildRequest(TripType.ROUND_TRIP, true);
        request.setReturnFlightId("RET-17");
        when(flightClient.getFlight("OUT-17")).thenReturn(Mono.just(flight("OUT-17", 5)));
        when(flightClient.getFlight("RET-17")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.bookFlight("OUT-17", request))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getTicket_returnsBookingWhenFound() {
        Booking booking = new Booking(
                "booking-5",
                TripType.ONE_WAY,
                "OUT-5",
                null,
                "PNR123",
                null,
                "Jane Doe",
                "jane@example.com",
                1,
                BookingStatus.CONFIRMED
        );

        when(bookingRepository.findByPnrOutbound("PNR123")).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.getTicket("PNR123"))
                .expectNext(booking)
                .verifyComplete();
    }

    @Test
    void getTicket_notFoundThrowsResourceNotFound() {
        when(bookingRepository.findByPnrOutbound("MISSING")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.getTicket("MISSING"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void cancelTicket_marksCancelledAndReleasesSeats() {
        Booking booking = new Booking(
                "booking-6",
                TripType.ROUND_TRIP,
                "OUT-6",
                "RET-6",
                "PNR456",
                "PNR789",
                "Alex Roe",
                "alex@example.com",
                2,
                BookingStatus.CONFIRMED
        );

        when(bookingRepository.findByPnrOutbound("PNR456")).thenReturn(Mono.just(booking));
        when(flightClient.releaseSeats(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.cancelTicket("PNR456"))
                .expectNextMatches(result -> "Booking cancelled".equals(result.get("message")))
                .verifyComplete();

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(captor.capture());
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());

        verify(flightClient, times(1)).releaseSeats("OUT-6", 2);
        verify(flightClient, times(1)).releaseSeats("RET-6", 2);
    }

    @Test
    void cancelTicket_oneWayReleasesOnlyOutbound() {
        Booking booking = new Booking(
                "booking-8",
                TripType.ONE_WAY,
                "OUT-8",
                null,
                "PNR111",
                null,
                "Taylor",
                "taylor@example.com",
                1,
                BookingStatus.CONFIRMED
        );

        when(bookingRepository.findByPnrOutbound("PNR111")).thenReturn(Mono.just(booking));
        when(flightClient.releaseSeats("OUT-8", 1)).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.cancelTicket("PNR111"))
                .expectNextMatches(map -> map.get("message").equals("Booking cancelled"))
                .verifyComplete();

        verify(flightClient, times(1)).releaseSeats("OUT-8", 1);
        verify(flightClient, never()).releaseSeats(eq(null), anyInt());
    }

    @Test
    void cancelTicket_alreadyCancelledThrowsValidationException() {
        Booking booking = new Booking(
                "booking-7",
                TripType.ONE_WAY,
                "OUT-7",
                null,
                "PNR000",
                null,
                "Chris Smith",
                "chris@example.com",
                1,
                BookingStatus.CANCELLED
        );

        when(bookingRepository.findByPnrOutbound("PNR000")).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.cancelTicket("PNR000"))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Already cancelled", error.getMessage());
                })
                .verify();
    }

    @Test
    void cancelTicket_notFoundThrowsResourceNotFound() {
        when(bookingRepository.findByPnrOutbound("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.cancelTicket("UNKNOWN"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getHistory_emptyEmailThrowsValidationException() {
        StepVerifier.create(bookingService.getHistory("  "))
                .expectErrorSatisfies(error -> {
                    assertEquals(ValidationException.class, error.getClass());
                    assertEquals("Email cannot be empty", error.getMessage());
                })
                .verify();
    }

    private BookingRequest buildRequest(TripType tripType, boolean includeReturnFlight) {
        BookingRequest request = new BookingRequest();
        request.setContactName("John Doe");
        request.setContactEmail("john@example.com");
        request.setTripType(tripType);
        if (includeReturnFlight) {
            request.setReturnFlightId("RET-2");
        }
        request.setPassengers(List.of(
                passenger("Alice", "1A", includeReturnFlight ? "2A" : null),
                passenger("Bob", "1B", includeReturnFlight ? "2B" : null)
        ));
        return request;
    }

    private PassengerRequest passenger(String name, String seatOutbound, String seatReturn) {
        PassengerRequest passenger = new PassengerRequest();
        passenger.setName(name);
        passenger.setAge(30);
        passenger.setGender(Gender.MALE);
        passenger.setSeatOutbound(seatOutbound);
        passenger.setSeatReturn(seatReturn);
        return passenger;
    }

    private FlightDto flight(String id, int availableSeats) {
        FlightDto dto = new FlightDto();
        dto.setFlightId(id);
        dto.setAvailableSeats(availableSeats);
        return dto;
    }
}
