package com.bookingservice.model;

import java.util.List;

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
	private List<Passenger> passengers;
    private List<String> warnings;
	private String pnrReturn;
    private String contactName;
    private String contactEmail;
    private int totalPassengers;
    private BookingStatus status;
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}
	public TripType getTripType() {
		return tripType;
	}
	public void setTripType(TripType tripType) {
		this.tripType = tripType;
	}
	public String getOutboundFlightId() {
		return outboundFlightId;
	}
	public void setOutboundFlightId(String outboundFlightId) {
		this.outboundFlightId = outboundFlightId;
	}
	public String getReturnFlight() {
		return returnFlight;
	}
	public void setReturnFlight(String returnFlight) {
		this.returnFlight = returnFlight;
	}
	public String getPnrOutbound() {
		return pnrOutbound;
	}
	public void setPnrOutbound(String pnrOutbound) {
		this.pnrOutbound = pnrOutbound;
	}
	public List<Passenger> getPassengers() {
		return passengers;
	}
	public void setPassengers(List<Passenger> passengers) {
		this.passengers = passengers;
	}
	public List<String> getWarnings() {
		return warnings;
	}
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	public String getPnrReturn() {
		return pnrReturn;
	}
	public void setPnrReturn(String pnrReturn) {
		this.pnrReturn = pnrReturn;
	}
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
	public int getTotalPassengers() {
		return totalPassengers;
	}
	public void setTotalPassengers(int totalPassengers) {
		this.totalPassengers = totalPassengers;
	}
	public BookingStatus getStatus() {
		return status;
	}
	public void setStatus(BookingStatus status) {
		this.status = status;
	}
	
}
