package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertNotNull(b1);
        assertNotEquals(booking("other", "PNR2", "FL-2"), b1);
        assertTrue(b1.toString().contains("PNR1"));
    }

    @Test
    void booking_handlesNullFieldsInEquals() {
        Booking left = new Booking();
        Booking right = new Booking();

        assertEquals(left, right);
        right.setPnrReturn("X");
        assertNotEquals(left, right);
        right.setPnrReturn(null);
        assertEquals(left, right);
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
        assertNotNull(p1);
        assertNotEquals(passenger("other", "Other", "9Z"), p1);
        assertTrue(p1.toString().contains("John"));
        assertNotEquals(p1, p4);
        assertEquals(p1, p1);
    }

    @Test
    void passenger_handlesNullOptionalFields() {
        Passenger base = new Passenger();
        base.setPassengerId("P1");

        Passenger compare = new Passenger();
        compare.setPassengerId("P1");

        assertEquals(base, compare);
        compare.setSeatOutbound("1A");
        assertNotEquals(base, compare);
        base.setSeatOutbound("1A");
        assertEquals(base, compare);
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
        assertNotNull(e1);
        assertNotEquals(bookingEvent("other", now.plusSeconds(5)), e1);
        assertTrue(e1.toString().contains("b1"));
    }

    @Test
    void bookingEvent_handlesNulls() {
        BookingEvent a = new BookingEvent();
        BookingEvent b = new BookingEvent();
        b.setPnrReturn("X");
        assertNotEquals(a, b);
        b.setPnrReturn(null);
        assertEquals(a, b);
    }

    @Test
    void booking_coversAllFieldComparisons() {
        Booking allNull = new Booking();
        Booking sameNull = new Booking();
        assertEquals(allNull, sameNull);
        sameNull.setBookingId("ID");
        assertNotEquals(allNull, sameNull);

        Booking baseline = fullBooking();
        Booking equal = fullBooking();
        assertEquals(baseline, equal);
        assertNotNull(baseline);
        assertNotEquals(new Booking(), baseline);
        assertEquals(baseline, fullBooking());

        assertBookingNotEquals(b -> b.setBookingId("DIFF"));
        assertBookingNotEquals(b -> b.setTripType(TripType.ROUND_TRIP));
        assertBookingNotEquals(b -> b.setOutboundFlightId("OUT2"));
        assertBookingNotEquals(b -> b.setReturnFlight("RET2"));
        assertBookingNotEquals(b -> b.setPnrOutbound("PO2"));
        assertBookingNotEquals(b -> b.setPassengers(java.util.List.of()));
        assertBookingNotEquals(b -> b.setWarnings(java.util.List.of("warn2")));
        assertBookingNotEquals(b -> b.setPnrReturn("PR2"));
        assertBookingNotEquals(b -> b.setContactName("Other"));
        assertBookingNotEquals(b -> b.setContactEmail("other@example.com"));
        assertBookingNotEquals(b -> b.setTotalPassengers(99));
        assertBookingNotEquals(b -> b.setStatus(BookingStatus.CANCELLED));

        baseline.hashCode();
        equal.hashCode();
        new Booking().hashCode();
        assertNotEquals(baseline, new BookingSubclass());
    }

    @Test
    void passenger_coversAllFieldComparisons() {
        Passenger base = fullPassenger();
        Passenger same = fullPassenger();
        assertEquals(base, same);
        assertEquals(base, fullPassenger());

        assertPassengerNotEquals(p -> p.setPassengerId("DIFF"));
        assertPassengerNotEquals(p -> p.setBookingId("B2"));
        assertPassengerNotEquals(p -> p.setName("Other"));
        assertPassengerNotEquals(p -> p.setAge(99));
        assertPassengerNotEquals(p -> p.setGender(Gender.OTHER));
        assertPassengerNotEquals(p -> p.setMeal(Meal.NON_VEG));
        assertPassengerNotEquals(p -> p.setSeatOutbound("X1"));
        assertPassengerNotEquals(p -> p.setSeatReturn("X2"));

        base.hashCode();
        same.hashCode();
        new Passenger().hashCode();

        Passenger constructed = new Passenger("ID", "BID", "Name", 20, Gender.MALE, Meal.VEG, "1A", "2B");
        assertEquals("ID", constructed.getPassengerId());
        PassengerSubclass sub = new PassengerSubclass();
        assertNotEquals(base, sub);
        assertNotEquals(sub, base);
        assertNotEquals(new Passenger(), fullPassenger());
    }

    @Test
    void bookingEvent_coversAllFieldComparisons() {
        BookingEvent base = fullBookingEvent();
        BookingEvent same = fullBookingEvent();
        assertEquals(base, same);
        assertEquals(base, fullBookingEvent());

        assertBookingEventNotEquals(e -> e.setEventType(BookingEventType.CANCELLED));
        assertBookingEventNotEquals(e -> e.setBookingId("DIFF"));
        assertBookingEventNotEquals(e -> e.setPnrOutbound("PO2"));
        assertBookingEventNotEquals(e -> e.setPnrReturn("PR2"));
        assertBookingEventNotEquals(e -> e.setOutboundFlightId("OUT2"));
        assertBookingEventNotEquals(e -> e.setReturnFlightId("RET2"));
        assertBookingEventNotEquals(e -> e.setContactName("Name2"));
        assertBookingEventNotEquals(e -> e.setContactEmail("c2@example.com"));
        assertBookingEventNotEquals(e -> e.setTotalPassengers(5));
        assertBookingEventNotEquals(e -> e.setStatus(BookingStatus.CANCELLED));
        assertBookingEventNotEquals(e -> e.setTripType(TripType.ONE_WAY));
        assertBookingEventNotEquals(e -> e.setOccurredAt(java.time.Instant.EPOCH));

        base.hashCode();
        same.hashCode();
        new BookingEvent().hashCode();
        BookingEventSubclass sub = new BookingEventSubclass();
        assertNotEquals(base, sub);
        assertNotEquals(sub, base);
        assertNotEquals(new BookingEvent(), fullBookingEvent());
        BookingEvent empty = new BookingEvent();
        BookingEvent withId = new BookingEvent();
        withId.setBookingId("X");
        assertNotEquals(empty, withId);
    }

    private Booking fullBooking() {
        Booking b = new Booking();
        b.setBookingId("B1");
        b.setTripType(TripType.ONE_WAY);
        b.setOutboundFlightId("OUT");
        b.setReturnFlight("RET");
        b.setPnrOutbound("PO");
        b.setPassengers(java.util.List.of(new Passenger()));
        b.setWarnings(java.util.List.of("warn"));
        b.setPnrReturn("PR");
        b.setContactName("Name");
        b.setContactEmail("email@example.com");
        b.setTotalPassengers(2);
        b.setStatus(BookingStatus.CONFIRMED);
        return b;
    }

    private Passenger fullPassenger() {
        Passenger p = new Passenger();
        p.setPassengerId("P1");
        p.setBookingId("B1");
        p.setName("Name");
        p.setAge(30);
        p.setGender(Gender.MALE);
        p.setMeal(Meal.VEG);
        p.setSeatOutbound("1A");
        p.setSeatReturn("2B");
        return p;
    }

    private BookingEvent fullBookingEvent() {
        return new BookingEvent(
                BookingEventType.BOOKED,
                "B1",
                "PO",
                "PR",
                "OUT",
                "RET",
                "Name",
                "email@example.com",
                2,
                BookingStatus.CONFIRMED,
                TripType.ROUND_TRIP,
                java.time.Instant.parse("2025-01-01T00:00:00Z")
        );
    }

    private void assertBookingNotEquals(java.util.function.Consumer<Booking> mutator) {
        Booking copy = fullBooking();
        mutator.accept(copy);
        assertNotEquals(fullBooking(), copy);
    }

    private void assertPassengerNotEquals(java.util.function.Consumer<Passenger> mutator) {
        Passenger copy = fullPassenger();
        mutator.accept(copy);
        assertNotEquals(fullPassenger(), copy);
    }

    private void assertBookingEventNotEquals(java.util.function.Consumer<BookingEvent> mutator) {
        BookingEvent copy = fullBookingEvent();
        mutator.accept(copy);
        assertNotEquals(fullBookingEvent(), copy);
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

    private static class BookingSubclass extends Booking {
        @Override
        public boolean canEqual(Object other) {
            return false;
        }
    }

    private static class PassengerSubclass extends Passenger {
        @Override
        public boolean canEqual(Object other) {
            return false;
        }
    }

    private static class BookingEventSubclass extends BookingEvent {
        @Override
        public boolean canEqual(Object other) {
            return false;
        }
    }
}
