package com.apigateway.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.apigateway.dto.PasswordResetConfirmRequest;
import com.apigateway.dto.PasswordResetRequest;
import com.apigateway.kafka.PasswordResetProducer;
import com.apigateway.model.PasswordResetToken;
import com.apigateway.model.User;
import com.apigateway.repository.PasswordResetTokenRepository;
import com.apigateway.repository.UserRepository;

import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetProducer producer;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordResetProducer producer,
            @Value("${password.reset.code.ttl-minutes:10}") long ttlMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.producer = producer;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public Mono<String> requestReset(PasswordResetRequest request) {
        return userRepository.findByUsername(request.username())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> {
                    if (request.email() != null && !request.email().equals(user.email())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email does not match"));
                    }
                    String code = generateCode();
                    Instant expiresAt = Instant.now().plus(ttl);
                    PasswordResetToken token = new PasswordResetToken(
                            null,
                            user.username(),
                            code,
                            expiresAt,
                            false
                    );
                    return tokenRepository.save(token)
                            .doOnSuccess(saved -> producer.sendEvent(user.username(), user.email(), code, expiresAt))
                            .thenReturn("Reset code sent");
                });
    }

    public Mono<String> confirmReset(PasswordResetConfirmRequest request) {
        return userRepository.findByUsername(request.username())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> tokenRepository
                        .findFirstByUsernameAndCodeAndUsedFalseOrderByExpiresAtDesc(request.username(), request.code())
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code")))
                        .flatMap(token -> {
                            if (token.expiresAt().isBefore(Instant.now())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired"));
                            }
                            if (encoder.matches(request.newPassword(), user.password())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must differ from old"));
                            }

                            PasswordResetToken used = new PasswordResetToken(
                                    token.id(),
                                    token.username(),
                                    token.code(),
                                    token.expiresAt(),
                                    true
                            );

                            User updatedUser = new User(
                                    user.id(),
                                    user.username(),
                                    encoder.encode(request.newPassword()),
                                    user.fullName(),
                                    user.email(),
                                    user.role()
                            );

                            return tokenRepository.save(used)
                                    .then(userRepository.save(updatedUser))
                                    .thenReturn("Password updated successfully");
                        })
                );
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
