package com.bookingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
	
	@Id
	private String bookingId;	
	private TripType tripType;
	private String outboundFlightId; //fk-> flightinventory
	private String returnFlight;
	private String pnrOutbound;
    private String pnrReturn;
    private String contactName;
    private String contactEmail;
    private int totalPassengers;
    private BookingStatus status;
}
