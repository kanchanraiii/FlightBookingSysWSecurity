package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class BookingEventTest {

    @Test
    void holdsValues() {
        Instant now = Instant.now();
        BookingEvent event = new BookingEvent(
                BookingEventType.BOOKED,
                "b1",
                "PNR1",
                "PNR2",
                "OUT1",
                "RET1",
                "Alice",
                "alice@example.com",
                2,
                BookingStatus.CONFIRMED,
                TripType.ROUND_TRIP,
                now
        );

        assertEquals(BookingEventType.BOOKED, event.getEventType());
        assertEquals("b1", event.getBookingId());
        assertEquals("PNR1", event.getPnrOutbound());
        assertEquals("PNR2", event.getPnrReturn());
        assertEquals("OUT1", event.getOutboundFlightId());
        assertEquals("RET1", event.getReturnFlightId());
        assertEquals("Alice", event.getContactName());
        assertEquals("alice@example.com", event.getContactEmail());
        assertEquals(2, event.getTotalPassengers());
        assertEquals(BookingStatus.CONFIRMED, event.getStatus());
        assertEquals(TripType.ROUND_TRIP, event.getTripType());
        assertEquals(now, event.getOccurredAt());
    }
}
