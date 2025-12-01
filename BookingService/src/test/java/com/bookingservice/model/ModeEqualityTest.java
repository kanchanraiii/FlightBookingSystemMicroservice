package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModelEqualityTest {

    @Test
    void booking_equalsHashCodeAndToString() {
        Booking b1 = booking("b1", "PNR1", "FL-1");
        Booking b2 = booking("b1", "PNR1", "FL-1");
        Booking b3 = booking("b2", "PNR2", "FL-2");

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
        assertNotEquals(b1, null);
        assertNotEquals(b1, "string");
        assertTrue(b1.toString().contains("PNR1"));
    }

    @Test
    void passenger_equalsHashCodeAndToString() {
        Passenger p1 = passenger("p1", "John", "1A");
        Passenger p2 = passenger("p1", "John", "1A");
        Passenger p3 = passenger("p2", "Jane", "2B");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "string");
        assertTrue(p1.toString().contains("John"));
        assertTrue(p1.equals(p1)); // self check
    }

    private Booking booking(String id, String pnr, String outbound) {
        Booking b = new Booking();
        b.setBookingId(id);
        b.setTripType(TripType.ONE_WAY);
        b.setOutboundFlightId(outbound);
        b.setPnrOutbound(pnr);
        b.setStatus(BookingStatus.CONFIRMED);
        b.setTotalPassengers(1);
        b.setContactEmail("a@b.com");
        b.setContactName("Tester");
        return b;
    }

    private Passenger passenger(String id, String name, String seat) {
        Passenger p = new Passenger();
        p.setPassengerId(id);
        p.setBookingId("b1");
        p.setName(name);
        p.setAge(30);
        p.setGender(Gender.MALE);
        p.setMeal(Meal.VEG);
        p.setSeatOutbound(seat);
        p.setSeatReturn("2A");
        return p;
    }
}