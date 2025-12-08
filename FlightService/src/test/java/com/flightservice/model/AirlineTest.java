package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class AirlineTest {

    @Test
    void settersAndGettersWork() {
        Airline airline = new Airline();
        airline.setAirlineCode("AL1");
        airline.setAirlineName("Airline One");

        assertEquals("AL1", airline.getAirlineCode());
        assertEquals("Airline One", airline.getAirlineName());

        Airline same = new Airline();
        same.setAirlineCode("AL1");
        same.setAirlineName("Airline One");
        assertEquals(airline, same);
        assertEquals(airline.hashCode(), same.hashCode());

        assertNotNull(airline);              
        assertFalse(airline.equals("x"));    

        Airline different = new Airline();
        different.setAirlineCode("DIFF");
        different.setAirlineName("Other");
        assertNotEquals(airline, different);
        assertNotEquals(airline, mutate(same, a -> a.setAirlineName("Changed")));

        airline.toString();
    }

    private Airline mutate(Airline airline, java.util.function.Consumer<Airline> consumer) {
        consumer.accept(airline);
        return airline;
    }
}
