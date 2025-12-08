package com.flightservice.request;

import java.time.LocalDate;
import java.time.LocalTime;
import com.flightservice.model.Cities;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
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
}
