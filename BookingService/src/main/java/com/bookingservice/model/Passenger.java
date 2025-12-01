package com.bookingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("passengers")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Passenger {

    @Id
    private String passengerId;
    private String bookingId; // fk -> booking
    private String name;
    private int age;
    private Gender gender;
    private Meal meal;
    private String seatOutbound; 
    private String seatReturn;   
}
