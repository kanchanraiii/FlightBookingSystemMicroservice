package com.flightservice.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Document(collection="flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flights {
	@Id
	private String flightId;
	private String flightNumber;
	private String airlineCode; // fk -> airline
	private Cities sourceCity;
    private Cities destinationCity;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private boolean mealAvailable;
    private int totalSeats;
    private int availableSeats;
    private double price;
	

}
