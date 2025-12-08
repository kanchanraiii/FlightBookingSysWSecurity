package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

    @Test
    void equalsAndHashCodeCoverBranches() {
        Flights f1 = new Flights();
        f1.setFlightId("F1");
        f1.setFlightNumber("FN123");
        f1.setAirlineCode("AL1");
        f1.setSourceCity(Cities.DELHI);
        f1.setDestinationCity(Cities.MUMBAI);
        f1.setDepartureDate(LocalDate.of(2025, 1, 1));
        f1.setArrivalDate(LocalDate.of(2025, 1, 1));
        f1.setDepartureTime(LocalTime.of(10, 0));
        f1.setArrivalTime(LocalTime.of(12, 30));
        f1.setMealAvailable(false);
        f1.setTotalSeats(180);
        f1.setAvailableSeats(175);
        f1.setPrice(249.50);

        Flights f2 = new Flights();
        f2.setFlightId("F1");
        f2.setFlightNumber("FN123");
        f2.setAirlineCode("AL1");
        f2.setSourceCity(Cities.DELHI);
        f2.setDestinationCity(Cities.MUMBAI);
        f2.setDepartureDate(LocalDate.of(2025, 1, 1));
        f2.setArrivalDate(LocalDate.of(2025, 1, 1));
        f2.setDepartureTime(LocalTime.of(10, 0));
        f2.setArrivalTime(LocalTime.of(12, 30));
        f2.setMealAvailable(false);
        f2.setTotalSeats(180);
        f2.setAvailableSeats(175);
        f2.setPrice(249.50);

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, null);
        assertNotEquals(f1, "string");
        f2.setFlightId("DIFF");
        assertNotEquals(f1, f2);
        // flip each field to hit Lombok equals branches
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setFlightNumber("X")));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setAirlineCode("X")));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setSourceCity(Cities.BANGLORE)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setDestinationCity(Cities.CHENNAI)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setDepartureDate(LocalDate.of(2030, 1, 1))));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setArrivalDate(LocalDate.of(2030, 1, 1))));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setDepartureTime(LocalTime.MIDNIGHT)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setArrivalTime(LocalTime.NOON)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setMealAvailable(true)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setTotalSeats(999)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setAvailableSeats(1)));
        assertNotEquals(f1, mutate(copy(f1), fl -> fl.setPrice(1.23)));
        assertFalse(f1.toString().isEmpty());
    }

    private Flights copy(Flights original) {
        Flights f = new Flights();
        f.setFlightId(original.getFlightId());
        f.setFlightNumber(original.getFlightNumber());
        f.setAirlineCode(original.getAirlineCode());
        f.setSourceCity(original.getSourceCity());
        f.setDestinationCity(original.getDestinationCity());
        f.setDepartureDate(original.getDepartureDate());
        f.setArrivalDate(original.getArrivalDate());
        f.setDepartureTime(original.getDepartureTime());
        f.setArrivalTime(original.getArrivalTime());
        f.setMealAvailable(original.isMealAvailable());
        f.setTotalSeats(original.getTotalSeats());
        f.setAvailableSeats(original.getAvailableSeats());
        f.setPrice(original.getPrice());
        return f;
    }

    private Flights mutate(Flights f, java.util.function.Consumer<Flights> mutator) {
        mutator.accept(f);
        return f;
    }
}
