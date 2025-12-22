package com.apigateway.controller;

import com.apigateway.dto.AuthLoginRequest;
import com.apigateway.dto.AuthRegisterRequest;
import com.apigateway.dto.UpdatePasswordRequest;
import com.apigateway.dto.PasswordResetRequest;
import com.apigateway.dto.PasswordResetConfirmRequest;
import com.apigateway.model.User;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;
import com.apigateway.service.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository repo, JwtUtil jwtUtil, PasswordResetService passwordResetService) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
    }

    // to register a user
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register(@RequestBody AuthRegisterRequest request) {
        User user = new User(
                null,
                request.username(),
                encoder.encode(request.password()),
                request.fullName(),
                request.email(),
                request.role());
        return repo.save(user).thenReturn("User registered successfully");
    }

    // to login a user
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> login(@RequestBody AuthLoginRequest loginRequest) {
        return repo.findByUsername(loginRequest.username())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(user -> {
                    if (!encoder.matches(loginRequest.password(), user.password())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
                    }
                    String token = jwtUtil.generateToken(user.username(), user.role());
                    return Mono.just(token);
                });
    }
    
    // to update password
    @PutMapping("/update-password")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> updatePassword(@RequestBody UpdatePasswordRequest request) {
        if (request.newPassword() != null && request.newPassword().equals(request.oldPassword())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different"));
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .map(Authentication::getName)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication")))
                .flatMap(username -> repo.findByUsername(username)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                        .flatMap(user -> {
                            if (!encoder.matches(request.oldPassword(), user.password())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect"));
                            }

                            User updatedUser = new User(
                                    user.id(),
                                    user.username(),
                                    encoder.encode(request.newPassword()),
                                    user.fullName(),
                                    user.email(),
                                    user.role()
                            );

                            return repo.save(updatedUser).thenReturn("Password updated successfully");
                        })
                );
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        return passwordResetService.requestReset(request);
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        return passwordResetService.confirmReset(request);
    }


}
