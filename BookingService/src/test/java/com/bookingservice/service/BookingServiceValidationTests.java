package com.bookingservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bookingservice.client.FlightClient;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.TripType;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PassengerRepository;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;
import reactor.test.StepVerifier;

class BookingServiceValidationTests {

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                mock(BookingRepository.class),
                mock(PassengerRepository.class),
                mock(FlightClient.class),
                mock(BookingEventProducer.class),
                mock(EmailService.class));
    }

    @Test
    void bookFlightFailsWhenPassengersMissing() {
        BookingRequest req = new BookingRequest();
        req.setTripType(TripType.ONE_WAY);
        assertThatThrownBy(() -> bookingService.bookFlight("F1", req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Passenger required");
    }

    @Test
    void bookFlightFailsWhenPassengersEmpty() {
        BookingRequest req = new BookingRequest();
        req.setTripType(TripType.ONE_WAY);
        req.setPassengers(java.util.Collections.emptyList());

        assertThatThrownBy(() -> bookingService.bookFlight("F1", req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Passenger required");
    }

    @Test
    void bookFlightFailsWhenTripTypeMissing() {
        BookingRequest req = new BookingRequest();
        req.setPassengers(Collections.singletonList(new PassengerRequest()));
        assertThatThrownBy(() -> bookingService.bookFlight("F1", req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trip type required");
    }

    @Test
    void getHistoryRejectsEmptyEmail() {
        StepVerifier.create(bookingService.getHistory(""))
                .verifyError(ValidationException.class);
    }

    @Test
    void getHistoryRejectsNullEmail() {
        StepVerifier.create(bookingService.getHistory(null))
                .verifyError(ValidationException.class);
    }
}
