package com.apigateway.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.apigateway.model.PasswordResetToken;

import reactor.core.publisher.Mono;

public interface PasswordResetTokenRepository extends ReactiveMongoRepository<PasswordResetToken, String> {
    Mono<PasswordResetToken> findFirstByUsernameAndCodeAndUsedFalseOrderByExpiresAtDesc(String username, String code);
}
