package com.apigateway.controller;

import com.apigateway.dto.AuthLoginRequest;
import com.apigateway.dto.AuthRegisterRequest;
import com.apigateway.dto.UpdatePasswordRequest;
import com.apigateway.model.User;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
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
    public Mono<String> updatePassword(
            @RequestBody UpdatePasswordRequest request
    ) {
        return repo.findByUsername(request.username())
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                ))
                .flatMap(user -> {

                    // email check
                    if (!user.email().equals(request.email())) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Email does not match"
                                )
                        );
                    }

                    // old password check
                    if (!encoder.matches(request.oldPassword(), user.password())) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Old password is incorrect"
                                )
                        );
                    }

                    // update password (record - create new instance)
                    User updatedUser = new User(
                            user.id(),
                            user.username(),
                            encoder.encode(request.newPassword()),
                            user.fullName(),
                            user.email(),
                            user.role()
                    );

                    return repo.save(updatedUser)
                            .thenReturn("Password updated successfully");
                });
    }


}
