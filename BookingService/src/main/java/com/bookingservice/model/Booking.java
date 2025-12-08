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
    @org.springframework.data.annotation.Transient
    private List<String> warnings;
	private String pnrReturn;
    private String contactName;
    private String contactEmail;
    private int totalPassengers;
    private BookingStatus status;
}
