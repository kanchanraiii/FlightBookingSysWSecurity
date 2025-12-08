package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ModelCoverageTest {

    @Test
    void passenger_gettersAndSettersWork() {
        Passenger passenger = new Passenger();
        passenger.setPassengerId("p1");
        passenger.setBookingId("b1");
        passenger.setName("John");
        passenger.setAge(30);
        passenger.setGender(Gender.MALE);
        passenger.setMeal(Meal.VEG);
        passenger.setSeatOutbound("1A");
        passenger.setSeatReturn("2B");

        assertEquals("p1", passenger.getPassengerId());
        assertEquals("b1", passenger.getBookingId());
        assertEquals("John", passenger.getName());
        assertEquals(30, passenger.getAge());
        assertEquals(Gender.MALE, passenger.getGender());
        assertEquals(Meal.VEG, passenger.getMeal());
        assertEquals("1A", passenger.getSeatOutbound());
        assertEquals("2B", passenger.getSeatReturn());
    }

    @Test
    void booking_gettersAndSettersWork() {
        Booking booking = new Booking();
        booking.setBookingId("b1");
        booking.setTripType(TripType.ROUND_TRIP);
        booking.setOutboundFlightId("OUT-1");
        booking.setReturnFlight("RET-1");
        booking.setPnrOutbound("PNR1");
        booking.setPnrReturn("PNR2");
        booking.setContactName("Jane");
        booking.setContactEmail("jane@example.com");
        booking.setTotalPassengers(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setWarnings(java.util.List.of("warn1", "warn2"));

        assertEquals("b1", booking.getBookingId());
        assertEquals(TripType.ROUND_TRIP, booking.getTripType());
        assertEquals("OUT-1", booking.getOutboundFlightId());
        assertEquals("RET-1", booking.getReturnFlight());
        assertEquals("PNR1", booking.getPnrOutbound());
        assertEquals("PNR2", booking.getPnrReturn());
        assertEquals("Jane", booking.getContactName());
        assertEquals("jane@example.com", booking.getContactEmail());
        assertEquals(2, booking.getTotalPassengers());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(2, booking.getWarnings().size());

        Booking constructed = new Booking(
                "b2",
                TripType.ONE_WAY,
                "OUT-2",
                null,
                "PNR3",
                java.util.List.of(),
                java.util.List.of("w1"),
                null,
                "Bob",
                "bob@example.com",
                1,
                BookingStatus.CONFIRMED
        );
        assertEquals("b2", constructed.getBookingId());
        assertEquals("bob@example.com", constructed.getContactEmail());
    }

    @Test
    void mealEnum_hasValues() {
        assertNotNull(Meal.valueOf("VEG"));
        assertEquals(2, Meal.values().length);
    }
}
