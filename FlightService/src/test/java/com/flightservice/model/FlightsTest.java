package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class FlightsTest {

    @Test
    void settersAndGettersWork() {
        Flights flight = new Flights();
        flight.setFlightId("F1");
        flight.setFlightNumber("FN123");
        flight.setAirlineCode("AL1");
        flight.setSourceCity(Cities.DELHI);
        flight.setDestinationCity(Cities.MUMBAI);
        flight.setDepartureDate(LocalDate.of(2025, 1, 1));
        flight.setArrivalDate(LocalDate.of(2025, 1, 1));
        flight.setDepartureTime(LocalTime.of(10, 0));
        flight.setArrivalTime(LocalTime.of(12, 30));
        flight.setMealAvailable(false);
        flight.setTotalSeats(180);
        flight.setAvailableSeats(175);
        flight.setPrice(249.50);

        assertEquals("F1", flight.getFlightId());
        assertEquals("FN123", flight.getFlightNumber());
        assertEquals("AL1", flight.getAirlineCode());
        assertEquals(Cities.DELHI, flight.getSourceCity());
        assertEquals(Cities.MUMBAI, flight.getDestinationCity());
        assertEquals(LocalDate.of(2025, 1, 1), flight.getDepartureDate());
        assertEquals(LocalDate.of(2025, 1, 1), flight.getArrivalDate());
        assertEquals(LocalTime.of(10, 0), flight.getDepartureTime());
        assertEquals(LocalTime.of(12, 30), flight.getArrivalTime());
        assertFalse(flight.isMealAvailable());
        assertEquals(180, flight.getTotalSeats());
        assertEquals(175, flight.getAvailableSeats());
        assertEquals(249.50, flight.getPrice());
    }
}
