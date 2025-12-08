package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.flightservice.request.AddAirlineRequest;
import com.flightservice.request.SearchFlightRequest;

class DtoCoverageTest {

    @Test
    void airline_andSeats_accessorsWork() {
        Airline airline = new Airline();
        airline.setAirlineName("TestAir");
        airline.setAirlineCode("TA");

        Seats seats = new Seats();
        seats.setSeatId("S1");
        seats.setSeatNo("12A");
        seats.setAvailable(true);
        seats.setBooked(false);
        seats.setFlightId("FL1");

        assertEquals("TestAir", airline.getAirlineName());
        assertEquals("TA", airline.getAirlineCode());
        assertEquals("S1", seats.getSeatId());
        assertEquals("12A", seats.getSeatNo());
        assertEquals("FL1", seats.getFlightId());
        assertEquals(Boolean.TRUE, seats.isAvailable());
        assertEquals(Boolean.FALSE, seats.isBooked());
    }

    @Test
    void requestDtos_coverAllFields() {
        AddAirlineRequest add = new AddAirlineRequest();
        add.setAirlineName("Name");
        add.setAirlineCode("CODE");

        SearchFlightRequest search = new SearchFlightRequest();
        search.setSourceCity(Cities.DELHI);
        search.setDestinationCity(Cities.MUMBAI);
        search.setTravelDate(LocalDate.of(2025, 1, 1));
        search.setReturnDate(LocalDate.of(2025, 1, 10));
        search.setTripType(TripType.ROUND_TRIP);

        assertEquals("Name", add.getAirlineName());
        assertEquals("CODE", add.getAirlineCode());
        assertEquals("Name", add.getAirlineName());
        assertEquals("CODE", add.getAirlineCode());

        assertEquals(Cities.DELHI, search.getSourceCity());
        assertEquals(Cities.MUMBAI, search.getDestinationCity());
        assertEquals(LocalDate.of(2025, 1, 1), search.getTravelDate());
        assertEquals(LocalDate.of(2025, 1, 10), search.getReturnDate());
        assertEquals(TripType.ROUND_TRIP, search.getTripType());
        assertNotNull(search.toString());
    }
}
