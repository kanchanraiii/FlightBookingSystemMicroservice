package com.flightservice.request;

import java.time.LocalDate;
import java.time.LocalTime;
import com.flightservice.model.Cities;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddFlightRequest {
	
	@NotBlank(message="Airline code is a required field")
	private String airlineCode;
	
	@NotBlank(message="Flight number is a required field")
	private String flightNumber;
	
	@NotNull(message="Source City cannot be empty")
	private Cities sourceCity;
	
	@NotNull(message="Destination city cannot be empty")
	private Cities destinationCity;
	
	@NotNull(message="Departure and Arrival date cannot be empty ")
	private LocalDate departureDate;
	
	@NotNull(message="Departure and Arrival time cannot be empty ")
	private LocalTime departureTime;
	
	@NotNull(message="Departure and Arrival date cannot be empty ")
	private LocalDate arrivalDate;
	
	@NotNull(message="Departure and Arrival time cannot be empty ")
	private LocalTime arrivalTime;
	
	@NotNull(message="Total seats cannot be empty")
	@Positive(message="Total Seats must be positive")
	private Integer totalSeats;
	
	@NotNull(message="Price cannot be empty")
	@Positive(message="Price must be positive")
	private Float price;
	
	private boolean mealAvailable;

	// getter and setters
	public String getAirlineCode() {
		return airlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}

	public Cities getSourceCity() {
		return sourceCity;
	}

	public void setSourceCity(Cities sourceCity) {
		this.sourceCity = sourceCity;
	}

	public Cities getDestinationCity() {
		return destinationCity;
	}

	public void setDestinationCity(Cities destinationCity) {
		this.destinationCity = destinationCity;
	}

	public LocalDate getDepartureDate() {
		return departureDate;
	}

	public void setDepartureDate(LocalDate departureDate) {
		this.departureDate = departureDate;
	}

	public LocalTime getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(LocalTime departureTime) {
		this.departureTime = departureTime;
	}

	public LocalDate getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(LocalDate arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public LocalTime getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Integer getTotalSeats() {
		return totalSeats;
	}

	public void setTotalSeats(Integer totalSeats) {
		this.totalSeats = totalSeats;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public boolean isMealAvailable() {
		return mealAvailable;
	}

	public void setMealAvailable(boolean mealAvailable) {
		this.mealAvailable = mealAvailable;
	}
	

}