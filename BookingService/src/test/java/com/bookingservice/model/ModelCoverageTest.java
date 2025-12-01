package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ModelCoverageTest {

    @Test
    void passenger_gettersAndSettersWork() {
        Passenger passenger = new Passenger();
        passenger.setPassengerId("p1");
        passenger.setBookingId("b1");
        passenger.setName("John");
        passenger.setAge(30);
        passenger.setGender(Gender.MALE);
        passenger.setMeal(Meal.VEG);
        passenger.setSeatOutbound("1A");
        passenger.setSeatReturn("2B");

        assertEquals("p1", passenger.getPassengerId());
        assertEquals("b1", passenger.getBookingId());
        assertEquals("John", passenger.getName());
        assertEquals(30, passenger.getAge());
        assertEquals(Gender.MALE, passenger.getGender());
        assertEquals(Meal.VEG, passenger.getMeal());
        assertEquals("1A", passenger.getSeatOutbound());
        assertEquals("2B", passenger.getSeatReturn());
    }

    @Test
    void booking_gettersAndSettersWork() {
        Booking booking = new Booking();
        booking.setBookingId("b1");
        booking.setTripType(TripType.ROUND_TRIP);
        booking.setOutboundFlightId("OUT-1");
        booking.setReturnFlight("RET-1");
        booking.setPnrOutbound("PNR1");
        booking.setPnrReturn("PNR2");
        booking.setContactName("Jane");
        booking.setContactEmail("jane@example.com");
        booking.setTotalPassengers(2);
        booking.setStatus(BookingStatus.CONFIRMED);

        assertEquals("b1", booking.getBookingId());
        assertEquals(TripType.ROUND_TRIP, booking.getTripType());
        assertEquals("OUT-1", booking.getOutboundFlightId());
        assertEquals("RET-1", booking.getReturnFlight());
        assertEquals("PNR1", booking.getPnrOutbound());
        assertEquals("PNR2", booking.getPnrReturn());
        assertEquals("Jane", booking.getContactName());
        assertEquals("jane@example.com", booking.getContactEmail());
        assertEquals(2, booking.getTotalPassengers());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void allArgsConstructorsPopulateFields() {
        Booking booking = new Booking(
                "b2",
                TripType.ONE_WAY,
                "OUT-2",
                null,
                "PNR3",
                null,
                "Alex",
                "alex@example.com",
                1,
                BookingStatus.CANCELLED);
        assertEquals("b2", booking.getBookingId());
        assertEquals("alex@example.com", booking.getContactEmail());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        Passenger passenger = new Passenger(
                "p2",
                "b2",
                "Chris",
                40,
                Gender.OTHER,
                Meal.NON_VEG,
                "3C",
                "4D");
        assertEquals("p2", passenger.getPassengerId());
        assertEquals(Meal.NON_VEG, passenger.getMeal());
        assertEquals("4D", passenger.getSeatReturn());
    }

    @Test
    void mealEnum_hasValues() {
        assertNotNull(Meal.valueOf("VEG"));
        assertEquals(2, Meal.values().length);
    }
}