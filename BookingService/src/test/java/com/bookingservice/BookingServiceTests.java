package com.bookingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.bookingservice.client.FlightClient;
import com.bookingservice.client.FlightDto;
import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.TripType;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PassengerRepository;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;
import com.bookingservice.service.BookingEventProducer;
import com.bookingservice.service.BookingService;
import com.bookingservice.service.EmailService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTests {

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

    BookingRequest request;
    FlightDto flightDto;

    @BeforeEach
    void setup() {
        request = new BookingRequest();
        request.setContactName("Jane Doe");
        request.setContactEmail("jane@example.com");
        request.setTripType(TripType.ONE_WAY);
        request.setPassengers(List.of(passenger("A"), passenger("B")));

        flightDto = new FlightDto();
        flightDto.setFlightId("F1");
        flightDto.setAvailableSeats(5);

        when(eventProducer.publish(any())).thenReturn(Mono.just(true));
        when(emailService.sendBookingNotification(any(), any())).thenReturn(Mono.empty());
        when(passengerRepository.saveAll(any(Iterable.class))).thenReturn(Flux.empty());
        when(passengerRepository.saveAll(any(org.reactivestreams.Publisher.class))).thenReturn(Flux.empty());
        when(passengerRepository.deleteByBookingId(anyString())).thenReturn(Mono.empty());
        when(bookingRepository.deleteById(anyString())).thenReturn(Mono.empty());
        when(flightClient.releaseSeats(anyString(), anyInt())).thenReturn(Mono.empty());
        when(flightClient.reserveSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        when(passengerRepository.findByBookingId(anyString())).thenReturn(Flux.empty());
        when(bookingRepository.existsByOutboundFlightIdAndPassengersNameAndPassengersAge(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(false));
    }

    private PassengerRequest passenger(String name) {
        PassengerRequest p = new PassengerRequest();
        p.setName(name);
        p.setAge(25);
        p.setGender(Gender.FEMALE);
        p.setSeatOutbound("S1");
        return p;
    }

    @Test
    void bookFlight_oneWay_success() {
        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.reserveSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            ReflectionTestUtils.setField(b, "bookingId", "B1");
            return Mono.just(b);
        });
        StepVerifier.create(bookingService.bookFlight("F1", request))
                .assertNext(b -> {
                    assertEquals(BookingStatus.CONFIRMED, b.getStatus());
                    assertNotNull(b.getPnrOutbound());
                    assertEquals(6, b.getPnrOutbound().length());
                })
                .verifyComplete();

        verify(flightClient, times(1)).reserveSeatNumbers("F1", List.of("S1", "S1"));
        verify(eventProducer, times(1)).publish(any());
        verify(emailService, times(1)).sendBookingNotification(any(), any());
    }

    @Test
    void bookFlight_notEnoughSeats() {
        flightDto.setAvailableSeats(1);
        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(ValidationException.class);
    }

    @Test
    void bookFlight_missingPassengersThrows() {
        request.setPassengers(null);
        org.junit.jupiter.api.Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.bookFlight("F1", request));
    }

    @Test
    void bookFlight_returnFlightMissingThrows() {
        request.setTripType(TripType.ROUND_TRIP);
        org.junit.jupiter.api.Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.bookFlight("F1", request));
    }

    @Test
    void bookFlight_roundTrip_success() {
        request.setTripType(TripType.ROUND_TRIP);
        request.setReturnFlightId("F2");
        request.getPassengers().forEach(p -> p.setSeatReturn("R1"));

        FlightDto returnDto = new FlightDto();
        returnDto.setFlightId("F2");
        returnDto.setAvailableSeats(5);

        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.getFlight("F2")).thenReturn(Mono.just(returnDto));
        when(flightClient.reserveSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            ReflectionTestUtils.setField(b, "bookingId", "B2");
            return Mono.just(b);
        });

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .assertNext(b -> {
                    assertEquals(TripType.ROUND_TRIP, b.getTripType());
                    assertNotNull(b.getPnrReturn());
                })
                .verifyComplete();

        verify(flightClient, times(1)).reserveSeatNumbers("F1", List.of("S1", "S1"));
        verify(flightClient, times(1)).reserveSeatNumbers("F2", List.of("R1", "R1"));
        verify(eventProducer, times(1)).publish(any());
        verify(emailService, times(1)).sendBookingNotification(any(), any());
    }

    @Test
    void bookFlight_roundTripMissingSeatReturn() {
        request.setTripType(TripType.ROUND_TRIP);
        request.setReturnFlightId("F2");
        request.getPassengers().forEach(p -> p.setSeatReturn(null));

        FlightDto returnDto = new FlightDto();
        returnDto.setFlightId("F2");
        returnDto.setAvailableSeats(5);

        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.getFlight("F2")).thenReturn(Mono.just(returnDto));

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(ValidationException.class);
    }

    @Test
    void bookFlight_invalidPassengerAge() {
        request.getPassengers().get(0).setAge(0);
        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(ValidationException.class);
    }

    @Test
    void bookFlight_missingOutboundSeat() {
        request.getPassengers().get(0).setSeatOutbound(null);
        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(ValidationException.class);
    }

    @Test
    void bookFlight_sideEffectsFailureIsSwallowed() {
        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.reserveSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        when(eventProducer.publish(any())).thenReturn(Mono.just(false));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            ReflectionTestUtils.setField(b, "bookingId", "B3");
            return Mono.just(b);
        });

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void bookFlight_returnFlightNotFound() {
        request.setTripType(TripType.ROUND_TRIP);
        request.setReturnFlightId("F2");
        request.getPassengers().forEach(p -> p.setSeatReturn("R1"));

        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.getFlight("F2")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    void bookFlight_returnNotEnoughSeats() {
        request.setTripType(TripType.ROUND_TRIP);
        request.setReturnFlightId("F2");
        request.getPassengers().forEach(p -> p.setSeatReturn("R1"));

        FlightDto returnDto = new FlightDto();
        returnDto.setFlightId("F2");
        returnDto.setAvailableSeats(1);

        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.getFlight("F2")).thenReturn(Mono.just(returnDto));

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .verifyError(ValidationException.class);
    }

    @Test
    void cancelTicket_success() {
        Booking booking = new Booking();
        booking.setBookingId("B1");
        booking.setOutboundFlightId("F1");
        booking.setTotalPassengers(2);
        booking.setStatus(BookingStatus.CONFIRMED);

        com.bookingservice.model.Passenger p1 = new com.bookingservice.model.Passenger();
        p1.setSeatOutbound("S1");
        com.bookingservice.model.Passenger p2 = new com.bookingservice.model.Passenger();
        p2.setSeatOutbound("S2");

        when(bookingRepository.findByPnrOutbound("PNR123")).thenReturn(Mono.just(booking));
        when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
        when(passengerRepository.findByBookingId("B1")).thenReturn(Flux.just(p1, p2));
        when(flightClient.releaseSeatNumbers(eq("F1"), anyList())).thenReturn(Mono.empty());
        StepVerifier.create(bookingService.cancelTicket("PNR123"))
                .assertNext(map -> assertEquals("Booking cancelled", map.get("message")))
                .verifyComplete();

        verify(eventProducer, times(1)).publish(any());
        verify(emailService, times(1)).sendBookingNotification(any(), any());
        verify(flightClient, times(1)).releaseSeatNumbers("F1", List.of("S1", "S2"));
    }

    @Test
    void cancelTicket_withReturn_releasesBoth() {
        Booking booking = new Booking();
        booking.setBookingId("B1");
        booking.setOutboundFlightId("F1");
        booking.setReturnFlight("F2");
        booking.setTotalPassengers(2);
        booking.setStatus(BookingStatus.CONFIRMED);

        com.bookingservice.model.Passenger p1 = new com.bookingservice.model.Passenger();
        p1.setSeatOutbound("S1");
        p1.setSeatReturn("R1");
        com.bookingservice.model.Passenger p2 = new com.bookingservice.model.Passenger();
        p2.setSeatOutbound("S2");
        p2.setSeatReturn("R2");

        when(bookingRepository.findByPnrOutbound("PNR123")).thenReturn(Mono.just(booking));
        when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
        when(passengerRepository.findByBookingId("B1")).thenReturn(Flux.just(p1, p2));
        when(flightClient.releaseSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        StepVerifier.create(bookingService.cancelTicket("PNR123"))
                .assertNext(map -> assertEquals("Booking cancelled", map.get("message")))
                .verifyComplete();

        verify(flightClient, times(1)).releaseSeatNumbers("F1", List.of("S1", "S2"));
        verify(flightClient, times(1)).releaseSeatNumbers("F2", List.of("R1", "R2"));
    }

    @Test
    void cancelTicket_alreadyCancelled() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByPnrOutbound("PNR123")).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.cancelTicket("PNR123"))
                .verifyError(ValidationException.class);
    }

    @Test
    void cancelTicket_notFound() {
        when(bookingRepository.findByPnrOutbound("PNR404")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.cancelTicket("PNR404"))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    void getTicket_notFound() {
        when(bookingRepository.findByPnrOutbound("PNR123")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.getTicket("PNR123"))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    void history_emptyEmail() {
        StepVerifier.create(bookingService.getHistory(" "))
                .verifyError(ValidationException.class);
    }

    @Test
    void history_success() {
        when(bookingRepository.findByContactEmail("a@b.com"))
                .thenReturn(Flux.just(new Booking()));

        StepVerifier.create(bookingService.getHistory("a@b.com"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void bookFlight_emailFailureAddsWarning() {
        request.setPassengers(List.of(passenger("Warned")));
        when(flightClient.getFlight("F1")).thenReturn(Mono.just(flightDto));
        when(flightClient.reserveSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            ReflectionTestUtils.setField(b, "bookingId", "BWARN");
            return Mono.just(b);
        });
        when(emailService.sendBookingNotification(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("smtp down")));

        StepVerifier.create(bookingService.bookFlight("F1", request))
                .assertNext(b -> assertEquals(1, b.getWarnings().size()))
                .verifyComplete();
    }

    @Test
    void cancelTicket_kafkaDownAddsWarning() {
        Booking booking = new Booking();
        booking.setBookingId("B1");
        booking.setOutboundFlightId("F1");
        booking.setTotalPassengers(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByPnrOutbound("PNR-KAF")).thenReturn(Mono.just(booking));
        when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
        com.bookingservice.model.Passenger p1 = new com.bookingservice.model.Passenger();
        p1.setSeatOutbound("S1");
        com.bookingservice.model.Passenger p2 = new com.bookingservice.model.Passenger();
        p2.setSeatOutbound("S2");
        when(passengerRepository.findByBookingId("B1")).thenReturn(Flux.just(p1, p2));
        when(flightClient.releaseSeatNumbers(anyString(), anyList())).thenReturn(Mono.empty());
        when(eventProducer.publish(any())).thenReturn(Mono.just(false));

        StepVerifier.create(bookingService.cancelTicket("PNR-KAF"))
                .assertNext(map -> {
                    assertEquals("Booking cancelled", map.get("message"));
                    assertNotNull(map.get("warning"));
                })
                .verifyComplete();
    }
}
