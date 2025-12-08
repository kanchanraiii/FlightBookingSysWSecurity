package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModelEqualityTest {

    @Test
    void booking_equalsHashCodeAndToString() {
        Booking b1 = booking("b1", "PNR1", "FL-1");
        Booking b2 = booking("b1", "PNR1", "FL-1");
        Booking b3 = booking("b2", "PNR2", "FL-2");

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
        assertNotEquals(null, b1);
        assertNotEquals("string", b1);
        assertTrue(b1.toString().contains("PNR1"));
    }

    @Test
    void passenger_equalsHashCodeAndToString() {
        Passenger p1 = passenger("p1", "John", "1A");
        Passenger p2 = passenger("p1", "John", "1A");
        Passenger p3 = passenger("p2", "Jane", "2B");
        Passenger p4 = passenger("p1", "John", "DIFF");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(null, p1);
        assertNotEquals("string", p1);
        assertTrue(p1.toString().contains("John"));
        assertNotEquals(p1, p4);
        assertEquals(p1, p1); // self check
    }

    @Test
    void bookingEvent_equalsHashCodeAndToString() {
        java.time.Instant now = java.time.Instant.now();
        BookingEvent e1 = bookingEvent("b1", now);
        BookingEvent e2 = bookingEvent("b1", now);
        BookingEvent e3 = bookingEvent("b2", now.plusSeconds(1));

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
        assertNotEquals(null, e1);
        assertNotEquals("string", e1);
        assertTrue(e1.toString().contains("b1"));
    }

    private Booking booking(String id, String pnr, String outbound) {
        Booking b = new Booking();
        b.setBookingId(id);
        b.setTripType(TripType.ONE_WAY);
        b.setOutboundFlightId(outbound);
        b.setPnrOutbound(pnr);
        b.setStatus(BookingStatus.CONFIRMED);
        b.setTotalPassengers(1);
        b.setContactEmail("a@b.com");
        b.setContactName("Tester");
        return b;
    }

    private Passenger passenger(String id, String name, String seat) {
        Passenger p = new Passenger();
        p.setPassengerId(id);
        p.setBookingId("b1");
        p.setName(name);
        p.setAge(30);
        p.setGender(Gender.MALE);
        p.setMeal(Meal.VEG);
        p.setSeatOutbound(seat);
        p.setSeatReturn("2A");
        return p;
    }

    private BookingEvent bookingEvent(String bookingId, java.time.Instant occurredAt) {
        return new BookingEvent(
                BookingEventType.BOOKED,
                bookingId,
                "PNR1",
                "PNR2",
                "OUT1",
                "RET1",
                "Alice",
                "alice@example.com",
                2,
                BookingStatus.CONFIRMED,
                TripType.ROUND_TRIP,
                occurredAt
        );
    }
}
