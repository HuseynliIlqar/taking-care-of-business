package com.travel.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class CarRentalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarRentalServiceApplication.class, args);
    }
}
