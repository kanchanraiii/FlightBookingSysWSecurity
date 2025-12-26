package com.apigateway.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PasswordResetEmailListener {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailListener.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final String from;

    public PasswordResetEmailListener(
            JavaMailSender mailSender,
            ObjectMapper objectMapper,
            @Value("${password.reset.mail.from:no-reply@example.com}") String from) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.from = from;
    }

    @KafkaListener(
            topics = "${password.reset.topic:password-reset-events}",
            groupId = "${spring.kafka.consumer.group-id:password-reset-emailer}",
            autoStartup = "${password.reset.listener.enabled:true}"
    )
    public void handleResetEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.path("email").asText(null);
            String code = node.path("code").asText(null);
            String username = node.path("username").asText("");
            String expiresAt = node.path("expiresAt").asText("");

            if (email == null || code == null) {
                log.warn("Password reset event missing email or code: {}", message);
                return;
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                mail.setFrom(from);
            } else {
                log.warn("No valid from address configured; using default transport sender address");
            }
            mail.setTo(email);
            mail.setSubject("Your password reset code");
            mail.setText("Hi " + username + ",\n\n"
                    + "Your password reset code is: " + code + "\n"
                    + "It expires at: " + expiresAt + "\n\n"
                    + "If you did not request this, you can ignore this email.");

            mailSender.send(mail);
            log.info("Sent password reset email to {}", email);
        } catch (Exception ex) {
            log.error("Failed to process password reset event: {}", message, ex);
        }
    }
}
