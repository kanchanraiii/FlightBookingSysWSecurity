package com.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

	@Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**", "/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/flight/api/flight/getAllAirlines",
                                "/flight/api/flight/getAllFlights").permitAll()
                        .pathMatchers(HttpMethod.POST, "/flight/api/flight/search").permitAll()
                        .pathMatchers(HttpMethod.POST,
                                "/flight/api/flight/addAirline",
                                "/flight/api/flight/airline/inventory/add").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/booking/api/booking/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET,
                                "/booking/api/booking/ticket/**",
                                "/booking/api/booking/history/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/booking/api/booking/cancel/**").hasRole("USER")
                        .anyExchange().authenticated()
                )
                
                .build();
    }
}
