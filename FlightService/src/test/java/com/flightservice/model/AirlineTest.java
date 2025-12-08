package com.flightservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AirlineTest {

    @Test
    void settersAndGettersWork() {
        Airline airline = new Airline();
        airline.setAirlineCode("AL1");
        airline.setAirlineName("Airline One");

        assertEquals("AL1", airline.getAirlineCode());
        assertEquals("Airline One", airline.getAirlineName());
    }
}
