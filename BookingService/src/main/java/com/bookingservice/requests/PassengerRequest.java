package com.bookingservice.requests;

import com.bookingservice.model.Gender;
import com.bookingservice.model.Meal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PassengerRequest {

    @NotBlank(message="Name is a required field")
    private String name;

    @NotNull(message="Age is a required field")
    @Min(value=1, message="Min Age can be 1")
    private Integer age;

    @NotNull(message="Gender cannot be empty")
    private Gender gender; 

    private String seatOutbound; 
    private String seatReturn;

    private Meal meal;

    // getters and setters 
    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public Integer getAge() { 
        return age; 
    }
    public void setAge(Integer age) { 
        this.age = age; 
    }

    public Gender getGender() { 
        return gender; 
    }
    public void setGender(Gender gender) { 
        this.gender = gender; 
    }

    public String getSeatOutbound() { 
        return seatOutbound; 
    }
    public void setSeatOutbound(String seatOutbound) { 
        this.seatOutbound = seatOutbound; 
    }

    public String getSeatReturn() { 
        return seatReturn; 
    }
    public void setSeatReturn(String seatReturn) { 
        this.seatReturn = seatReturn; 
    }

    public Meal getMeal() { 
        return meal; 
    }
    public void setMeal(Meal meal) { 
        this.meal = meal; 
    }
}
