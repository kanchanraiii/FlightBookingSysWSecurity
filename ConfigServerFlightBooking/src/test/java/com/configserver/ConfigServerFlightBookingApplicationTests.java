package com.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ConfigServerFlightBookingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ConfigServerFlightBookingApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context starts for the config server
    }
}
