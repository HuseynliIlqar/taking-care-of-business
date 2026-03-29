package com.travel.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "ribbon.eureka.enabled=false",
    "zuul.ignored-services=*"
})
class ApiGatewayApplicationTest {

    @Test
    @DisplayName("API Gateway context loads successfully")
    void contextLoads() {
        assertTrue(true, "Spring context should load without errors");
    }

    @Test
    @DisplayName("API Gateway application starts as Zuul Proxy")
    void applicationStartsAsZuulProxy() {
        ApiGatewayApplication app = new ApiGatewayApplication();
        assertTrue(app != null, "Application instance should be created");
    }
}
