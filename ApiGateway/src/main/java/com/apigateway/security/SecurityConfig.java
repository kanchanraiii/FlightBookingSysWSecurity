package com.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/flight/api/flight/search").permitAll()
                        .pathMatchers("/flight/api/flight/getAllFlights").permitAll()
                        .pathMatchers("/flight/api/flight/getFlight/**").permitAll()
                        .pathMatchers("/flight/api/flight/getAllAirlines").permitAll()
                        .anyExchange().authenticated()
                )
                
                .build();
    }
}
