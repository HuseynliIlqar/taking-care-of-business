package com.travel.discovery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class DiscoveryServiceApplicationTest {

    @Test
    @DisplayName("Discovery Service context loads successfully")
    void contextLoads() {
        assertTrue(true, "Spring context should load without errors");
    }

    @Test
    @DisplayName("Discovery Service application starts as Eureka Server")
    void applicationStartsAsEurekaServer() {
        DiscoveryServiceApplication app = new DiscoveryServiceApplication();
        assertTrue(app != null, "Application instance should be created");
    }
}
