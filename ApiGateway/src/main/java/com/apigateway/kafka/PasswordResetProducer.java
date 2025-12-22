package com.apigateway.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

@Component
public class PasswordResetProducer {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PasswordResetProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${password.reset.topic:password-reset-events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void sendEvent(String username, String email, String code, Instant expiresAt) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "username", username,
                            "email", email,
                            "code", code,
                            "expiresAt", expiresAt.toString()
                    )
            );
            kafkaTemplate.send(topic, username, payload);
            log.info("Password reset event published for user {}", username);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize password reset event for {}", username, e);
        }
    }
}
