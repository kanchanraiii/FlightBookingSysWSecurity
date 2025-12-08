package com.bookingservice.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class WebClientConfigTest {

    @Test
    void webClientBuilderIsCreated() {
        WebClient.Builder builder = new WebClientConfig().webClientBuilder();
        assertNotNull(builder);
        assertNotNull(builder.build());
    }
}
