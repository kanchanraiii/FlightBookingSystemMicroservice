package com.bookingservice.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookingservice.client.FlightClient;
import com.bookingservice.client.FlightDto;
import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingEvent;
import com.bookingservice.model.BookingEventType;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.TripType;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PassengerRepository;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    private final PassengerRepository passengerRepository;

    private final FlightClient flightClient;

    private final BookingEventProducer eventProducer;

    private final EmailService emailService;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            FlightClient flightClient,
            BookingEventProducer eventProducer,
            EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.flightClient = flightClient;
        this.eventProducer = eventProducer;
        this.emailService = emailService;
    }

   // to book a flight
    public Mono<Booking> bookFlight(String flightId, BookingRequest req) {

        validatePassengersExist(req);
        validateTripType(req);

        int count = req.getPassengers().size();

        Mono<FlightDto> outboundMono =
                flightClient.getFlight(flightId)
                        .switchIfEmpty(Mono.error(
                                new ResourceNotFoundException("Outbound flight not found")));

        if (req.getReturnFlightId() == null) {
            return outboundMono.flatMap(flight -> {

                validateSeats(flight.getAvailableSeats(), count);
                validatePassengers(req, false);

                return createBooking(req, flightId, null)
                        .flatMap(saved ->
                                flightClient.reserveSeats(flightId, count)
                                        .then(savePassengers(req, saved))
                                        .then(emitSideEffects(saved, BookingEventType.BOOKED))
                                        .thenReturn(saved));
            });
        }

        Mono<FlightDto> returnMono =
                flightClient.getFlight(req.getReturnFlightId())
                        .switchIfEmpty(Mono.error(
                                new ResourceNotFoundException("Return flight not found")));

        return outboundMono.zipWith(returnMono)
                .flatMap(t -> {

                    validateSeats(t.getT1().getAvailableSeats(), count);
                    validateSeats(t.getT2().getAvailableSeats(), count);

                    validatePassengers(req, true);

                    return createBooking(req, flightId, req.getReturnFlightId())
                            .flatMap(saved ->
                                    flightClient.reserveSeats(flightId, count)
                                            .then(flightClient.reserveSeats(req.getReturnFlightId(), count))
                                            .then(savePassengers(req, saved))
                                            .then(emitSideEffects(saved, BookingEventType.BOOKED))
                                            .thenReturn(saved));
                });
    }

    // to create a booking
    private Mono<Booking> createBooking(
            BookingRequest req,
            String outboundId,
            String returnId) {

        Booking booking = new Booking();
        booking.setOutboundFlightId(outboundId);
        booking.setReturnFlight(returnId);
        booking.setTripType(req.getTripType());
        booking.setContactName(req.getContactName());
        booking.setContactEmail(req.getContactEmail());
        booking.setTotalPassengers(req.getPassengers().size());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPnrOutbound(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        if (returnId != null) {
            booking.setPnrReturn(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }

        return bookingRepository.save(booking);
    }

   // to save passengers 
    private Mono<Void> savePassengers(BookingRequest req, Booking booking) {
        return passengerRepository
                .saveAll(Flux.fromIterable(req.getPassengers())
                        .map(p -> toPassenger(p, booking.getBookingId())))
                .then();
    }

    private com.bookingservice.model.Passenger toPassenger(
            PassengerRequest p, String bookingId) {

        com.bookingservice.model.Passenger passenger =
                new com.bookingservice.model.Passenger();

        passenger.setName(p.getName());
        passenger.setAge(p.getAge());
        passenger.setGender(p.getGender());
        passenger.setSeatOutbound(p.getSeatOutbound());
        passenger.setSeatReturn(p.getSeatReturn());
        passenger.setBookingId(bookingId);

        return passenger;
    }

    // to get ticket
    public Mono<Booking> getTicket(String pnr) {
        return bookingRepository.findByPnrOutbound(pnr)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("PNR not found")));
    }

   // to cancel a booking
    public Mono<Map<String, String>> cancelTicket(String pnr) {

        return bookingRepository.findByPnrOutbound(pnr)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("PNR not found")))
                .flatMap(booking -> {

                    if (booking.getStatus() == BookingStatus.CANCELLED) {
                        return Mono.error(
                                new ValidationException("Already cancelled"));
                    }

                    booking.setStatus(BookingStatus.CANCELLED);

                    Mono<Void> releaseOutbound =
                            flightClient.releaseSeats(
                                    booking.getOutboundFlightId(),
                                    booking.getTotalPassengers());

                    Mono<Void> releaseReturn =
                            booking.getReturnFlight() == null
                                    ? Mono.empty()
                                    : flightClient.releaseSeats(
                                            booking.getReturnFlight(),
                                            booking.getTotalPassengers());

                    return bookingRepository.save(booking)
                            .flatMap(saved -> Mono.when(releaseOutbound, releaseReturn)
                                    .then(emitSideEffects(saved, BookingEventType.CANCELLED))
                                    .thenReturn(Map.of(
                                            "message", "Booking cancelled")));
                });
    }

    // validations
    private void validatePassengersExist(BookingRequest req) {
        if (req.getPassengers() == null || req.getPassengers().isEmpty()) {
            throw new ValidationException("Passenger required");
        }
    }

    private void validateTripType(BookingRequest req) {
        if (req.getTripType() == null) {
            throw new ValidationException("Trip type required");
        }
        if (req.getTripType() == TripType.ROUND_TRIP
                && req.getReturnFlightId() == null) {
            throw new ValidationException("Return flight required");
        }
    }

    private void validateSeats(int available, int needed) {
        if (available < needed) {
            throw new ValidationException("Not enough seats");
        }
    }

    private void validatePassengers(BookingRequest req, boolean round) {
        req.getPassengers().forEach(p -> {
            if (p.getAge() <= 0) {
                throw new ValidationException("Invalid age");
            }
            if (p.getSeatOutbound() == null) {
                throw new ValidationException("Outbound seat required");
            }
            if (round && p.getSeatReturn() == null) {
                throw new ValidationException("Return seat required");
            }
        });
    }

    public Flux<Booking> getHistory(String email) {

        if (email == null || email.isBlank()) {
            return Flux.error(new ValidationException("Email cannot be empty"));
        }

        return bookingRepository.findByContactEmail(email);
    }

    private Mono<Void> emitSideEffects(Booking booking, BookingEventType type) {
        BookingEvent event = toEvent(booking, type);
        return Mono.when(
                        eventProducer.publish(event),
                        emailService.sendBookingNotification(booking, type)
                )
                .onErrorResume(ex -> Mono.empty());
    }

    private BookingEvent toEvent(Booking booking, BookingEventType type) {
        BookingEvent event = new BookingEvent();
        event.setEventType(type);
        event.setBookingId(booking.getBookingId());
        event.setPnrOutbound(booking.getPnrOutbound());
        event.setPnrReturn(booking.getPnrReturn());
        event.setOutboundFlightId(booking.getOutboundFlightId());
        event.setReturnFlightId(booking.getReturnFlight());
        event.setContactName(booking.getContactName());
        event.setContactEmail(booking.getContactEmail());
        event.setTotalPassengers(booking.getTotalPassengers());
        event.setStatus(booking.getStatus());
        event.setTripType(booking.getTripType());
        event.setOccurredAt(Instant.now());
        return event;
    }
}
