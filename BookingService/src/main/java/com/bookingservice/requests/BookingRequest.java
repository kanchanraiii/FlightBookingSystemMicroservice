package com.bookingservice.requests;

import java.util.List;

import com.bookingservice.model.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String contactEmail;

    @NotNull(message = "Trip type is required")
    private TripType tripType;

    private String returnFlightId;

    @Valid
    @NotNull(message = "Passenger list cannot be empty")
    private List<PassengerRequest> passengers;

    public String getContactName() {
        return contactName;
    }
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public TripType getTripType() {
        return tripType;
    }
    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public String getReturnFlightId() {
        return returnFlightId;
    }
    public void setReturnFlightId(String returnFlightId) {
        this.returnFlightId = returnFlightId;
    }

    public List<PassengerRequest> getPassengers() {
        return passengers;
    }
    public void setPassengers(List<PassengerRequest> passengers) {
        this.passengers = passengers;
    }
}
