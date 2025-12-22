package com.flightservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.flightservice.controller.MainController;
import com.flightservice.model.Flights;
import com.flightservice.request.AddFlightRequest;
import com.flightservice.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class MainControllerFlightTests {

    private FlightService flightService;
    private MainController controller;

    @BeforeEach
    void setup() {
        flightService = mock(FlightService.class);
        controller = new MainController(null, flightService, null);
    }

    @Test
    @DisplayName("Add flight delegates to service and returns response map")
    void addFlight() {
        when(flightService.addInventory(any(AddFlightRequest.class)))
                .thenReturn(Mono.just(Map.of("flightId", "F123")));

        StepVerifier.create(controller.addFlight(new AddFlightRequest()))
                .expectNextMatches((Map<String, String> map) -> "F123".equals(map.get("flightId")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Get all flights delegates to service")
    void getAllFlights() {
        Flights flight = new Flights();
        flight.setFlightId("FL1");
        when(flightService.getAllFlights()).thenReturn(Flux.just(flight));

        StepVerifier.create(controller.getAllFlights())
                .expectNextMatches(f -> "FL1".equals(f.getFlightId()))
                .verifyComplete();
    }
}
