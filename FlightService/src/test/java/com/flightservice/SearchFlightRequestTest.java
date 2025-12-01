package com.flightservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import com.flightservice.model.Cities;
import com.flightservice.model.TripType;
import com.flightservice.request.SearchFlightRequest;

import org.junit.jupiter.api.Test;

class SearchFlightRequestTest {

    @Test
    void testGettersAndSetters() {
        SearchFlightRequest req = new SearchFlightRequest();

        LocalDate travelDate = LocalDate.now().plusDays(1);
        LocalDate returnDate = LocalDate.now().plusDays(5);

        req.setSourceCity(Cities.DELHI);
        req.setDestinationCity(Cities.MUMBAI);
        req.setTravelDate(travelDate);
        req.setTripType(TripType.ONE_WAY);
        req.setReturnDate(returnDate);

        assertEquals(Cities.DELHI, req.getSourceCity());
        assertEquals(Cities.MUMBAI, req.getDestinationCity());
        assertEquals(travelDate, req.getTravelDate());
        assertEquals(TripType.ONE_WAY, req.getTripType());
        assertEquals(returnDate, req.getReturnDate());
    }
}
