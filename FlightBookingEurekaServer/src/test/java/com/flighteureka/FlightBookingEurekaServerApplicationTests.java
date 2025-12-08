package com.flighteureka;

import static org.mockito.Mockito.mockStatic;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class FlightBookingEurekaServerApplicationTests {

    @Test
    void mainStartsWithoutLaunchingServer() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            FlightBookingEurekaServerApplication.main(new String[]{"--spring.main.web-application-type=none"});
            mocked.verify(() -> SpringApplication.run(FlightBookingEurekaServerApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void applicationClassInstantiates() {
        assertNotNull(new FlightBookingEurekaServerApplication());
    }
}
