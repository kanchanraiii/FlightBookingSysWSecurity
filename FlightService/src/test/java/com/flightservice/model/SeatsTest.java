package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        Seats same = new Seats();
        same.setSeatId("S1");
        same.setFlightId("F1");
        same.setSeatNo("12A");
        same.setAvailable(false);
        same.setBooked(true);

        assertEquals(same, seats);
        assertEquals(seats.hashCode(), same.hashCode());
        assertNotEquals(null, seats);
        assertNotEquals("x", seats);
        assertNotEquals(seats, mutate(copy(seats), s -> s.setSeatId("DIFF")));
        assertNotEquals(seats, mutate(copy(seats), s -> s.setFlightId("DIFF")));
        assertNotEquals(seats, mutate(copy(seats), s -> s.setSeatNo("1B")));
        assertNotEquals(seats, mutate(copy(seats), s -> s.setAvailable(true)));
        assertNotEquals(seats, mutate(copy(seats), s -> s.setBooked(false)));
        seats.toString();
    }

    private Seats copy(Seats s) {
        Seats clone = new Seats();
        clone.setSeatId(s.getSeatId());
        clone.setFlightId(s.getFlightId());
        clone.setSeatNo(s.getSeatNo());
        clone.setAvailable(s.isAvailable());
        clone.setBooked(s.isBooked());
        return clone;
    }

    private Seats mutate(Seats s, java.util.function.Consumer<Seats> c) {
        c.accept(s);
        return s;
    }
}
