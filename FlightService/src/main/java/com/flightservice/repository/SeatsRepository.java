package com.flightservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightservice.model.Seats;

import reactor.core.publisher.Flux;
import java.util.List;

public interface SeatsRepository extends ReactiveMongoRepository<Seats,String>{

    Flux<Seats> findByFlightId(String flightId);

    Flux<Seats> findByFlightIdAndSeatNoIn(String flightId, List<String> seatNos);
}
