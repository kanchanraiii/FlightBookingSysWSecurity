package com.bookingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.bookingservice.model.BookingEvent;

import java.time.Duration;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BookingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private final String topic;

    public BookingEventProducer(
            KafkaTemplate<String, BookingEvent> kafkaTemplate,
            @Value("${booking.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public Mono<Boolean> publish(BookingEvent event) {
        if (event == null || topic == null || topic.isBlank()) {
            return Mono.just(true);
        }
        return Mono.fromCallable(() -> kafkaTemplate.send(topic, event))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(5))
                .flatMap(Mono::fromFuture)
                .timeout(Duration.ofSeconds(5))
                .map(ignored -> true)
                .doOnError(ex -> log.error("Failed to publish booking event", ex))
                .onErrorResume(ex -> Mono.just(false));
    }
}
