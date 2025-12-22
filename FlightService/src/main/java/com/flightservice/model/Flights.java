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
	public Object getFlightId() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getFlightNumber() {
		return flightNumber;
	}
	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}
	public String getAirlineCode() {
		return airlineCode;
	}
	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
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
	public LocalDate getArrivalDate() {
		return arrivalDate;
	}
	public void setArrivalDate(LocalDate arrivalDate) {
		this.arrivalDate = arrivalDate;
	}
	public LocalTime getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(LocalTime departureTime) {
		this.departureTime = departureTime;
	}
	public LocalTime getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public boolean isMealAvailable() {
		return mealAvailable;
	}
	public void setMealAvailable(boolean mealAvailable) {
		this.mealAvailable = mealAvailable;
	}
	public int getTotalSeats() {
		return totalSeats;
	}
	public void setTotalSeats(int totalSeats) {
		this.totalSeats = totalSeats;
	}
	public int getAvailableSeats() {
		return availableSeats;
	}
	public void setAvailableSeats(int availableSeats) {
		this.availableSeats = availableSeats;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}
	
}
