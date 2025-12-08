package com.flightservice.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.flightservice.model.Cities;

class AddFlightRequestTest {

    @Test
    void settersAndGettersWork() {
        AddFlightRequest req = new AddFlightRequest();
        req.setAirlineCode("AL1");
        req.setFlightNumber("FN123");
        req.setSourceCity(Cities.DELHI);
        req.setDestinationCity(Cities.MUMBAI);
        req.setDepartureDate(LocalDate.of(2025, 1, 1));
        req.setDepartureTime(LocalTime.of(10, 0));
        req.setArrivalDate(LocalDate.of(2025, 1, 1));
        req.setArrivalTime(LocalTime.of(12, 30));
        req.setTotalSeats(150);
        req.setPrice(199.99f);
        req.setMealAvailable(true);

        assertEquals("AL1", req.getAirlineCode());
        assertEquals("FN123", req.getFlightNumber());
        assertEquals(Cities.DELHI, req.getSourceCity());
        assertEquals(Cities.MUMBAI, req.getDestinationCity());
        assertEquals(LocalDate.of(2025, 1, 1), req.getDepartureDate());
        assertEquals(LocalTime.of(10, 0), req.getDepartureTime());
        assertEquals(LocalDate.of(2025, 1, 1), req.getArrivalDate());
        assertEquals(LocalTime.of(12, 30), req.getArrivalTime());
        assertEquals(150, req.getTotalSeats());
        assertEquals(199.99f, req.getPrice());
        assertTrue(req.isMealAvailable());
    }
}
