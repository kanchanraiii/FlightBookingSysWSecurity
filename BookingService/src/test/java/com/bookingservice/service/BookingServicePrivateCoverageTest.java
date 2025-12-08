package com.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookingservice.client.FlightClient;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingEventType;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.TripType;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PassengerRepository;
import com.bookingservice.requests.PassengerRequest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BookingServicePrivateCoverageTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    PassengerRepository passengerRepository;
    @Mock
    FlightClient flightClient;
    @Mock
    BookingEventProducer eventProducer;
    @Mock
    EmailService emailService;

    @InjectMocks
    BookingService bookingService;

    @Test
    void toPassengerMapsAllFields() throws Exception {
        PassengerRequest req = new PassengerRequest();
        req.setName("Name");
        req.setAge(21);
        req.setGender(Gender.FEMALE);
        req.setSeatOutbound("1A");
        req.setSeatReturn("2B");

        Method m = BookingService.class.getDeclaredMethod("toPassenger", PassengerRequest.class, String.class);
        m.setAccessible(true);
        com.bookingservice.model.Passenger passenger =
                (com.bookingservice.model.Passenger) m.invoke(bookingService, req, "BID");

        assertEquals("Name", passenger.getName());
        assertEquals(21, passenger.getAge());
        assertEquals(Gender.FEMALE, passenger.getGender());
        assertEquals("1A", passenger.getSeatOutbound());
        assertEquals("2B", passenger.getSeatReturn());
        assertEquals("BID", passenger.getBookingId());
    }

    @Test
    void responseWithWarningsHandlesEmptyAndNonEmpty() throws Exception {
        Method m = BookingService.class.getDeclaredMethod("responseWithWarnings", String.class, List.class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> noWarn =
                (Map<String, String>) m.invoke(bookingService, "base", List.of());
        assertEquals("base", noWarn.get("message"));

        @SuppressWarnings("unchecked")
        Map<String, String> withWarn =
                (Map<String, String>) m.invoke(bookingService, "base", List.of("warn1"));
        assertTrue(withWarn.get("warning").contains("warn1"));
    }

    @Test
    void rootMessageHandlesNullMessage() throws Exception {
        Method m = BookingService.class.getDeclaredMethod("rootMessage", Throwable.class);
        m.setAccessible(true);

        String fromNull =
                (String) m.invoke(bookingService, new RuntimeException((String) null));
        assertTrue(fromNull.contains("RuntimeException"));

        String fromCause =
                (String) m.invoke(bookingService, new RuntimeException("outer", new IllegalStateException("inner")));
        assertEquals("inner", fromCause);
    }

    @Test
    void rollbackBookingSwallowsErrorsWhenKafkaDown() throws Exception {
        Booking booking = new Booking();
        booking.setBookingId("BID");
        booking.setOutboundFlightId("OUT");
        booking.setTotalPassengers(1);
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setReturnFlight(null); // cover no-return branch

        when(passengerRepository.deleteByBookingId("BID")).thenReturn(Mono.error(new RuntimeException("p")));
        when(bookingRepository.deleteById("BID")).thenReturn(Mono.error(new RuntimeException("b")));
        when(flightClient.releaseSeats("OUT", 1)).thenReturn(Mono.error(new RuntimeException("f")));

        Method m = BookingService.class.getDeclaredMethod("rollbackBooking", Booking.class);
        m.setAccessible(true);
        Mono<Void> result = (Mono<Void>) m.invoke(bookingService, booking);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void emitSideEffectsAddsWarningsForKafkaDownAndEmailFailure() throws Exception {
        Booking booking = new Booking();
        booking.setBookingId("BID");
        booking.setOutboundFlightId("OUT");
        booking.setPnrOutbound("PNR");
        booking.setTotalPassengers(1);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTripType(TripType.ONE_WAY);
        booking.setContactEmail("a@b.com");

        when(eventProducer.publish(any())).thenReturn(Mono.just(false));
        when(emailService.sendBookingNotification(any(), any()))
                .thenReturn(Mono.error(new RuntimeException((String) null)));

        Method m = BookingService.class.getDeclaredMethod("emitSideEffects", Booking.class, com.bookingservice.model.BookingEventType.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<Object> mono = (Mono<Object>) m.invoke(bookingService, booking, BookingEventType.BOOKED);

        StepVerifier.create(mono)
                .assertNext(outcome -> {
                    String str = outcome.toString();
                    assertTrue(str.contains("kafkaOk=false"));
                })
                .verifyComplete();
    }
}
