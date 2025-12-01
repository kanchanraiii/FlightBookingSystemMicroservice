package com.flightservice.request;
import jakarta.validation.constraints.NotBlank;

public class AddAirlineRequest {

    @NotBlank(message="Airline Code is a required field")
    private String airlineCode; 

    @NotBlank(message="Airline is a required field")
    private String airlineName;

    // getters and setters
    public String getAirlineCode() {
        return airlineCode;
    }
    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getAirlineName() {
        return airlineName;
    }
    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }
}

