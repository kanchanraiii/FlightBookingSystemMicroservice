package com.bookingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.bookingservice.client.FlightDto;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingEvent;
import com.bookingservice.model.BookingEventType;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.Passenger;
import com.bookingservice.model.TripType;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;

class ModelCoverageTest {

    @Test
    void bookingModelAccessors() {
        Booking b = new Booking();
        b.setBookingId("B1");
        b.setOutboundFlightId("F1");
        b.setReturnFlight("F2");
        b.setPnrOutbound("PNR1");
        b.setPnrReturn("PNR2");
        b.setContactName("Jane");
        b.setContactEmail("jane@example.com");
        b.setTotalPassengers(2);
        b.setStatus(BookingStatus.CONFIRMED);
        b.setTripType(TripType.ROUND_TRIP);

        assertEquals("B1", b.getBookingId());
        assertEquals("PNR2", b.getPnrReturn());
    }

    @Test
    void passengerModelAccessors() {
        Passenger p = new Passenger();
        p.setPassengerId("P1");
        p.setBookingId("B1");
        p.setName("John");
        p.setAge(30);
        p.setGender(Gender.MALE);
        p.setSeatOutbound("S1");
        p.setSeatReturn("R1");

        assertEquals("John", p.getName());
        assertEquals("R1", p.getSeatReturn());
    }

    @Test
    void bookingEventAccessors() {
        BookingEvent event = new BookingEvent();
        event.setEventType(BookingEventType.BOOKED);
        event.setBookingId("B1");
        event.setPnrOutbound("PNR1");
        event.setPnrReturn("PNR2");
        event.setOutboundFlightId("F1");
        event.setReturnFlightId("F2");
        event.setContactName("Jane");
        event.setContactEmail("jane@example.com");
        event.setTotalPassengers(2);
        event.setStatus(BookingStatus.CONFIRMED);
        event.setTripType(TripType.ONE_WAY);
        event.setOccurredAt(java.time.Instant.now());

        assertEquals("B1", event.getBookingId());
        assertEquals(BookingEventType.BOOKED, event.getEventType());
    }

    @Test
    void dtoAndRequestAccessors() {
        FlightDto dto = new FlightDto();
        dto.setFlightId("F1");
        dto.setAvailableSeats(5);
        assertEquals(5, dto.getAvailableSeats());

        PassengerRequest pr = new PassengerRequest();
        pr.setName("A");
        pr.setAge(20);
        pr.setGender(Gender.FEMALE);
        pr.setSeatOutbound("S1");

        BookingRequest br = new BookingRequest();
        br.setContactName("Jane");
        br.setContactEmail("jane@example.com");
        br.setTripType(TripType.ONE_WAY);
        br.setPassengers(List.of(pr));

        assertNotNull(br.getPassengers());
        assertEquals("S1", br.getPassengers().get(0).getSeatOutbound());
    }
}
