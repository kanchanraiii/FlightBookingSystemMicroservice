package com.flightservice.model;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection="airline")
@Data
public class Airline {
	
	@Id
	@Indexed(unique=true)
	private String airlineCode;
	private String airlineName;

}
