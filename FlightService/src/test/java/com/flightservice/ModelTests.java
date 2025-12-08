package com.flightservice;

import com.flightservice.model.Airline;
import com.flightservice.model.Cities;
import com.flightservice.model.Seats;
import com.flightservice.model.TripType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTests {

    @Test
    void testAirlineModel() {
        Airline airline = new Airline();
        airline.setAirlineCode("AI");
        airline.setAirlineName("Air India");

        assertEquals("AI", airline.getAirlineCode());
        assertEquals("Air India", airline.getAirlineName());

        Airline same = new Airline();
        same.setAirlineCode("AI");
        same.setAirlineName("Air India");
        assertEquals(airline, same);
        assertEquals(airline.hashCode(), same.hashCode());
        same.setAirlineName("Different");
        assertNotEquals(airline, same);
        assertNotEquals(null, airline);
    }

    @Test
    void testSeatModel() {
        Seats seat = new Seats();
        seat.setSeatId("S1");
        seat.setSeatNo("12A");
        seat.setFlightId("FL1");
        seat.setAvailable(true);

        assertEquals("S1", seat.getSeatId());
        assertEquals("FL1", seat.getFlightId());
        assertEquals("12A", seat.getSeatNo());
        assertTrue(seat.isAvailable());

        Seats same = new Seats();
        same.setSeatId("S1");
        same.setFlightId("FL1");
        same.setSeatNo("12A");
        same.setAvailable(true);

        assertEquals(seat, same);
        assertEquals(seat.hashCode(), same.hashCode());
        same.setSeatNo("DIFF");
        assertNotEquals(seat, same);
        assertNotEquals(null, seat);
    }

   
   
    void testEnums() { 
        assertEquals(TripType.ONE_WAY, TripType.valueOf("ONE_WAY"));
        assertEquals(14, Cities.values().length);
    }
}
