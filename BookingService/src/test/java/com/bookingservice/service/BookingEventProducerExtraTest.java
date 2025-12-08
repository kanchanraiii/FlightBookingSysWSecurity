package com.bookingservice.service;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import com.bookingservice.model.BookingEvent;

import reactor.test.StepVerifier;

class BookingEventProducerExtraTest {

    @Test
    void returnsTrueWhenTopicBlank() {
        BookingEventProducer producer = new BookingEventProducer(mock(KafkaTemplate.class), "  ");
        StepVerifier.create(producer.publish(new BookingEvent()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void returnsTrueWhenEventNull() {
        BookingEventProducer producer = new BookingEventProducer(mock(KafkaTemplate.class), "topic");
        StepVerifier.create(producer.publish(null))
                .expectNext(true)
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void returnsFalseWhenKafkaFails() {
        KafkaTemplate<String, BookingEvent> kafka = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafka.send(Mockito.anyString(), Mockito.any(BookingEvent.class)))
                .thenThrow(new RuntimeException("down"));
        BookingEventProducer producer = new BookingEventProducer(kafka, "topic");

        StepVerifier.create(producer.publish(new BookingEvent()))
                .expectNext(false)
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void returnsTrueWhenKafkaSucceeds() {
        KafkaTemplate<String, BookingEvent> kafka = Mockito.mock(KafkaTemplate.class);
        org.springframework.kafka.support.SendResult<String, BookingEvent> sendResult =
                Mockito.mock(org.springframework.kafka.support.SendResult.class);
        java.util.concurrent.CompletableFuture<org.springframework.kafka.support.SendResult<String, BookingEvent>> future =
                java.util.concurrent.CompletableFuture.completedFuture(sendResult);
        Mockito.when(kafka.send(Mockito.anyString(), Mockito.any(BookingEvent.class)))
                .thenReturn(future);

        BookingEventProducer producer = new BookingEventProducer(kafka, "topic");
        StepVerifier.create(producer.publish(new BookingEvent()))
                .expectNext(true)
                .verifyComplete();
    }
}
