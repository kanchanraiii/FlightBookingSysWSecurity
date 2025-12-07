package com.apigateway.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.apigateway.model.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
    Mono<User> findById(String userId);
}
