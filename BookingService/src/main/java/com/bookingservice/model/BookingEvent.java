package com.bookingservice.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private BookingEventType eventType;
    private String bookingId;
    private String pnrOutbound;
    private String pnrReturn;
    private String outboundFlightId;
    private String returnFlightId;
    private String contactName;
    private String contactEmail;
    private int totalPassengers;
    private BookingStatus status;
    private TripType tripType;
    private Instant occurredAt;
}
