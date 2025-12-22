package com.bookingservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class AdditionalModelCoverageTest {

    @Test
    void booking_andPassenger_roundTripFieldsCovered() {
        Booking booking = new Booking();
        booking.setBookingId("B123");
        booking.setTripType(TripType.ROUND_TRIP);
        booking.setOutboundFlightId("OUT");
        booking.setReturnFlight("RET");
        booking.setPnrOutbound("PNRO");
        booking.setPnrReturn("PNRR");
        booking.setWarnings(List.of("warn"));
        booking.setContactName("Name");
        booking.setContactEmail("email@example.com");
        booking.setTotalPassengers(2);
        booking.setStatus(BookingStatus.CONFIRMED);

        Passenger passenger = new Passenger();
        passenger.setPassengerId("P1");
        passenger.setBookingId("B123");
        passenger.setName("John");
        passenger.setAge(25);
        passenger.setGender(Gender.MALE);
        passenger.setMeal(Meal.NON_VEG);
        passenger.setSeatOutbound("1A");
        passenger.setSeatReturn("2B");

        assertEquals("B123", booking.getBookingId());
        assertEquals("RET", booking.getReturnFlight());
        assertEquals("PNRR", booking.getPnrReturn());
        assertEquals(1, booking.getWarnings().size());

        assertEquals("P1", passenger.getPassengerId());
        assertEquals("B123", passenger.getBookingId());
        assertEquals("John", passenger.getName());
        assertEquals(25, passenger.getAge());
        assertEquals(Gender.MALE, passenger.getGender());
        assertEquals(Meal.NON_VEG, passenger.getMeal());
        assertEquals("1A", passenger.getSeatOutbound());
        assertEquals("2B", passenger.getSeatReturn());
    }

//    @Test
//    void bookingEvent_allFieldsCovered() {
//        Instant now = Instant.now();
//        BookingEvent event = new BookingEvent(
//                BookingEventType.CANCELLED,
//                "B1",
//                "PNR1",
//                "PNR2",
//                "OUT",
//                "RET",
//                "Contact",
//                "contact@example.com",
//                3,
//                BookingStatus.CANCELLED,
//                TripType.ONE_WAY,
//                now
//        );
//
//        assertEquals(BookingEventType.CANCELLED, event.getEventType());
//        assertEquals("B1", event.getBookingId());
//        assertEquals("PNR2", event.getPnrReturn());
//        assertEquals("RET", event.getReturnFlightId());
//        assertEquals("Contact", event.getContactName());
//        assertEquals("contact@example.com", event.getContactEmail());
//        assertEquals(3, event.getTotalPassengers());
//        assertEquals(BookingStatus.CANCELLED, event.getStatus());
//        assertEquals(TripType.ONE_WAY, event.getTripType());
//        assertEquals(now, event.getOccurredAt());
//        assertNotNull(event.toString());
//    }
}
