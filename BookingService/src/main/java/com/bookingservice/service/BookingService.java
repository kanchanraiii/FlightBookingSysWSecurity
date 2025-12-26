package com.bookingservice.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookingservice.client.FlightClient;
import com.bookingservice.client.FlightDto;
import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingEvent;
import com.bookingservice.model.BookingEventType;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.TripType;
import com.bookingservice.model.Passenger;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PassengerRepository;
import com.bookingservice.requests.BookingRequest;
import com.bookingservice.requests.PassengerRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    private final PassengerRepository passengerRepository;

    private final FlightClient flightClient;

    private final BookingEventProducer eventProducer;

    private final EmailService emailService;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            FlightClient flightClient,
            BookingEventProducer eventProducer,
            EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.flightClient = flightClient;
        this.eventProducer = eventProducer;
        this.emailService = emailService;
    }

   // to book a flight
    public Mono<Booking> bookFlight(String flightId, BookingRequest req) {

        validatePassengersExist(req);
        validateTripType(req);

        int count = req.getPassengers().size();

        Mono<FlightDto> outboundMono =
                flightClient.getFlight(flightId)
                        .switchIfEmpty(Mono.error(
                                new ResourceNotFoundException("Outbound flight not found")));

        if (req.getReturnFlightId() == null) {
            return outboundMono.flatMap(flight -> {

                validateSeats(flight.getAvailableSeats(), count);
                validatePassengers(req, false);

                return createBooking(req, flightId, null)
                        .flatMap(saved ->
                                flightClient.reserveSeatNumbers(flightId, outboundSeats(req))
                                        .then(savePassengers(req, saved))
                                        .then(emitSideEffects(saved, BookingEventType.BOOKED))
                                        .flatMap(outcome -> handleOutcome(saved, outcome)));
            });
        }

        Mono<FlightDto> returnMono =
                flightClient.getFlight(req.getReturnFlightId())
                        .switchIfEmpty(Mono.error(
                                new ResourceNotFoundException("Return flight not found")));

        return outboundMono.zipWith(returnMono)
                .flatMap(t -> {

                    validateSeats(t.getT1().getAvailableSeats(), count);
                    validateSeats(t.getT2().getAvailableSeats(), count);

                    validatePassengers(req, true);

                    return createBooking(req, flightId, req.getReturnFlightId())
                            .flatMap(saved ->
                                    flightClient.reserveSeatNumbers(flightId, outboundSeats(req))
                                            .then(flightClient.reserveSeatNumbers(req.getReturnFlightId(), returnSeats(req)))
                                            .then(savePassengers(req, saved))
                                            .then(emitSideEffects(saved, BookingEventType.BOOKED))
                                            .flatMap(outcome -> handleOutcome(saved, outcome)));
                });
    }

    // to create a booking
    private Mono<Booking> createBooking(BookingRequest req, String outboundId, String returnId) {

        return Flux.fromIterable(req.getPassengers())
                .flatMap(p -> bookingRepository
                        .existsByOutboundFlightIdAndPassengersNameAndPassengersAge(
                                outboundId,
                                p.getName(),
                                p.getAge()
                        )
                        .flatMap(exists -> {
                            boolean alreadyBooked = Boolean.TRUE.equals(exists);
                            return alreadyBooked
                                    ? Mono.<Void>error(new ValidationException(
                                            "Passenger " + p.getName() + " is already booked on this flight"
                                    ))
                                    : Mono.empty();
                        }))
                .then(Mono.defer(() -> {

                    Booking booking = new Booking();
                    booking.setOutboundFlightId(outboundId);
                    booking.setReturnFlight(returnId);
                    booking.setTripType(req.getTripType());
                    booking.setContactName(req.getContactName());
                    booking.setContactEmail(req.getContactEmail());
                    booking.setTotalPassengers(req.getPassengers().size());
                    booking.setStatus(BookingStatus.CONFIRMED);

                    booking.setPnrOutbound(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    if (returnId != null) {
                        booking.setPnrReturn(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    }

                    return bookingRepository.save(booking);
                }));
    }


   // to save passengers 
    private Mono<Void> savePassengers(BookingRequest req, Booking booking) {
        return passengerRepository
                .saveAll(Flux.fromIterable(req.getPassengers())
                        .map(p -> toPassenger(p, booking.getBookingId())))
                .then();
    }

    private com.bookingservice.model.Passenger toPassenger(
            PassengerRequest p, String bookingId) {

        com.bookingservice.model.Passenger passenger =
                new com.bookingservice.model.Passenger();

        passenger.setName(p.getName());
        passenger.setAge(p.getAge());
        passenger.setGender(p.getGender());
        passenger.setSeatOutbound(p.getSeatOutbound());
        passenger.setSeatReturn(p.getSeatReturn());
        passenger.setBookingId(bookingId);

        return passenger;
    }

    // to get ticket
    public Mono<Booking> getTicket(String pnr) {
        return bookingRepository.findByPnrOutbound(pnr)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("PNR not found")));
    }

   // to cancel a booking
    public Mono<Map<String, String>> cancelTicket(String pnr) {

        return bookingRepository.findByPnrOutbound(pnr)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("PNR not found")))
                .flatMap(booking -> {

                    if (booking.getStatus() == BookingStatus.CANCELLED) {
                        return Mono.error(
                                new ValidationException("Already cancelled"));
                    }

                    booking.setStatus(BookingStatus.CANCELLED);

                    return passengerRepository.findByBookingId(booking.getBookingId()).collectList()
                            .flatMap(passengers -> bookingRepository.save(booking)
                                    .flatMap(saved -> {
                                        java.util.List<String> outboundSeats = extractSeats(passengers, true);
                                        java.util.List<String> returnSeats = extractSeats(passengers, false);

                                        Mono<Void> releaseOutboundSeats = releaseSeatNumbersIfAny(saved.getOutboundFlightId(), outboundSeats);
                                        Mono<Void> releaseReturnSeats = saved.getReturnFlight() == null
                                                ? Mono.empty()
                                                : releaseSeatNumbersIfAny(saved.getReturnFlight(), returnSeats);

                                        return Mono.when(releaseOutboundSeats, releaseReturnSeats)
                                                .then(emitSideEffects(saved, BookingEventType.CANCELLED))
                                                .flatMap(outcome -> {
                                                    saved.setWarnings(outcome.warnings());
                                                    return Mono.just(responseWithWarnings("Booking cancelled", outcome.warnings()));
                                                });
                                    }));
                });
    }

    // validations
    private void validatePassengersExist(BookingRequest req) {
        if (req.getPassengers() == null || req.getPassengers().isEmpty()) {
            throw new ValidationException("Passenger required");
        }
    }

    private void validateTripType(BookingRequest req) {
        if (req.getTripType() == null) {
            throw new ValidationException("Trip type required");
        }
        if (req.getTripType() == TripType.ROUND_TRIP
                && req.getReturnFlightId() == null) {
            throw new ValidationException("Return flight required");
        }
    }

    private void validateSeats(int available, int needed) {
        if (available < needed) {
            throw new ValidationException("Not enough seats");
        }
    }

    private java.util.List<String> outboundSeats(BookingRequest req) {
        return req.getPassengers().stream()
                .map(PassengerRequest::getSeatOutbound)
                .collect(Collectors.toList());
    }

    private java.util.List<String> returnSeats(BookingRequest req) {
        return req.getPassengers().stream()
                .map(PassengerRequest::getSeatReturn)
                .collect(Collectors.toList());
    }

    private void validatePassengers(BookingRequest req, boolean round) {
        req.getPassengers().forEach(p -> {
            if (p.getAge() <= 0) {
                throw new ValidationException("Invalid age");
            }
            if (p.getSeatOutbound() == null) {
                throw new ValidationException("Outbound seat required");
            }
            if (round && p.getSeatReturn() == null) {
                throw new ValidationException("Return seat required");
            }
        });
    }

    public Flux<Booking> getHistory(String email) {

        if (email == null || email.isBlank()) {
            return Flux.error(new ValidationException("Email cannot be empty"));
        }

        return bookingRepository.findByContactEmail(email);
    }

    private Mono<SideEffectOutcome> emitSideEffects(Booking booking, BookingEventType type) {
        BookingEvent event = toEvent(booking, type);

        Mono<Boolean> kafkaOkMono = eventProducer.publish(event)
                .defaultIfEmpty(true);

        Mono<String> emailResult = emailService.sendBookingNotification(booking, type)
                .then(Mono.<String>empty())
                .onErrorResume(ex -> Mono.just("Stored in DB but email not sent: " + rootMessage(ex)));

        return Mono.zip(kafkaOkMono, emailResult.defaultIfEmpty(""))
                .map(tuple -> {
                    boolean kafkaOk = tuple.getT1();
                    java.util.List<String> warnings = new java.util.ArrayList<>();
                    if (!kafkaOk) {
                        warnings.add("Kafka server is down; event not published.");
                    }
                    if (tuple.getT2() != null && !tuple.getT2().isBlank()) {
                        warnings.add(tuple.getT2());
                    }
                    return new SideEffectOutcome(kafkaOk, warnings);
                });
    }

    private Map<String, String> responseWithWarnings(String baseMessage, java.util.List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return Map.of("message", baseMessage);
        }
        return Map.of(
                "message", baseMessage,
                "warning", String.join("; ", warnings)
        );
    }

    private Mono<Booking> handleOutcome(Booking booking, SideEffectOutcome outcome) {
        if (!outcome.kafkaOk()) {
            return rollbackBooking(booking)
                    .then(Mono.error(new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                            "Couldn't book ticket as Kafka server is down."
                    )));
        }
        booking.setWarnings(outcome.warnings());
        return Mono.just(booking);
    }

    private Mono<Void> rollbackBooking(Booking booking) {
        Mono<Void> deletePassengers = passengerRepository.deleteByBookingId(booking.getBookingId())
                .onErrorResume(ex -> Mono.empty());
        Mono<Void> deleteBooking = bookingRepository.deleteById(booking.getBookingId())
                .onErrorResume(ex -> Mono.empty());
        reactor.core.publisher.Flux<Passenger> passengerFlux =
                passengerRepository.findByBookingId(booking.getBookingId());
        if (passengerFlux == null) {
            passengerFlux = Flux.empty();
        }
        Mono<java.util.List<Passenger>> passengersMono = passengerFlux.collectList()
                .onErrorResume(ex -> Mono.just(java.util.Collections.emptyList()));

        Mono<Void> releaseOutbound = passengersMono.flatMap(passengers ->
                        releaseSeatNumbersIfAny(booking.getOutboundFlightId(), extractSeats(passengers, true)))
                .onErrorResume(ex -> Mono.empty());
        Mono<Void> releaseReturn = passengersMono.flatMap(passengers ->
                        releaseSeatNumbersIfAny(booking.getReturnFlight(), extractSeats(passengers, false)))
                .onErrorResume(ex -> Mono.empty());

        return Mono.when(deletePassengers, deleteBooking, releaseOutbound, releaseReturn).then();
    }

    private record SideEffectOutcome(boolean kafkaOk, java.util.List<String> warnings) {}

    private String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getMessage() != null ? cur.getMessage() : cur.toString();
    }

    private BookingEvent toEvent(Booking booking, BookingEventType type) {
        BookingEvent event = new BookingEvent();
        event.setEventType(type);
        event.setBookingId(booking.getBookingId());
        event.setPnrOutbound(booking.getPnrOutbound());
        event.setPnrReturn(booking.getPnrReturn());
        event.setOutboundFlightId(booking.getOutboundFlightId());
        event.setReturnFlightId(booking.getReturnFlight());
        event.setContactName(booking.getContactName());
        event.setContactEmail(booking.getContactEmail());
        event.setTotalPassengers(booking.getTotalPassengers());
        event.setStatus(booking.getStatus());
        event.setTripType(booking.getTripType());
        event.setOccurredAt(Instant.now());
        return event;
    }

    private java.util.List<String> extractSeats(java.util.List<Passenger> passengers, boolean outbound) {
        if (passengers == null) {
            return java.util.Collections.emptyList();
        }
        return passengers.stream()
                .map(p -> outbound ? p.getSeatOutbound() : p.getSeatReturn())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Mono<Void> releaseSeatNumbersIfAny(String flightId, java.util.List<String> seats) {
        if (flightId == null || seats == null || seats.isEmpty()) {
            return Mono.empty();
        }
        return flightClient.releaseSeatNumbers(flightId, seats);
    }
}
