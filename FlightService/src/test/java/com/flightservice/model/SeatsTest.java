package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class SeatsTest {

    @Test
    void settersAndGettersWork() {
        Seats seats = new Seats();
        seats.setSeatId("S1");
        seats.setFlightId("F1");
        seats.setSeatNo("12A");
        seats.setAvailable(false);
        seats.setBooked(true);

        assertEquals("S1", seats.getSeatId());
        assertEquals("F1", seats.getFlightId());
        assertEquals("12A", seats.getSeatNo());
        assertFalse(seats.isAvailable());
        assertEquals(true, seats.isBooked());
    }
}
