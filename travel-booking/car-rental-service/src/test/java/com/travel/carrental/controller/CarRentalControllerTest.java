package com.travel.carrental.controller;

import com.travel.carrental.model.Car;
import com.travel.carrental.service.CarRentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarRentalController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@DisplayName("CarRentalController")
class CarRentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarRentalService carRentalService;

    @BeforeEach
    void resetMocks() {
        reset(carRentalService);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Car buildCar(String id, String make, String model, String type,
                          String location, double pricePerDay, boolean available, int seats) {
        return new Car(id, make, model, type, location, pricePerDay, available, seats);
    }

    // -------------------------------------------------------------------------
    // GET /cars
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /cars")
    class GetAllCars {

        @Test
        @DisplayName("returns 200 and JSON array when cars exist")
        void returns200WithCarList() throws Exception {
            List<Car> cars = Arrays.asList(
                    buildCar("C001", "Toyota", "Camry", "Sedan", "LosAngeles", 59.99, true, 5),
                    buildCar("C002", "Ford", "Explorer", "SUV", "LosAngeles", 89.99, true, 7)
            );
            when(carRentalService.getAllCars()).thenReturn(cars);

            mockMvc.perform(get("/cars").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is("C001")))
                    .andExpect(jsonPath("$[0].make", is("Toyota")))
                    .andExpect(jsonPath("$[0].model", is("Camry")))
                    .andExpect(jsonPath("$[0].type", is("Sedan")))
                    .andExpect(jsonPath("$[0].location", is("LosAngeles")))
                    .andExpect(jsonPath("$[0].pricePerDay", is(59.99)))
                    .andExpect(jsonPath("$[0].available", is(true)))
                    .andExpect(jsonPath("$[0].seats", is(5)))
                    .andExpect(jsonPath("$[1].id", is("C002")));
        }

        @Test
        @DisplayName("returns 200 with empty array when no cars exist")
        void returns200WithEmptyList() throws Exception {
            when(carRentalService.getAllCars()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/cars").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("returns 200 with single-element array")
        void returns200WithSingleCar() throws Exception {
            when(carRentalService.getAllCars()).thenReturn(
                    Collections.singletonList(
                            buildCar("C001", "Toyota", "Camry", "Sedan", "LosAngeles", 59.99, true, 5)));

            mockMvc.perform(get("/cars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("delegates to CarRentalService exactly once")
        void delegatesToServiceOnce() throws Exception {
            when(carRentalService.getAllCars()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/cars"));

            verify(carRentalService, times(1)).getAllCars();
            verifyNoMoreInteractions(carRentalService);
        }

        @Test
        @DisplayName("response includes available flag set to false for unavailable cars")
        void serialisesAvailableFalse() throws Exception {
            when(carRentalService.getAllCars()).thenReturn(
                    Collections.singletonList(
                            buildCar("C012", "Cadillac", "Escalade", "Luxury", "Miami", 199.99, false, 7)));

            mockMvc.perform(get("/cars"))
                    .andExpect(jsonPath("$[0].available", is(false)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /cars/search  -- both params optional
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /cars/search")
    class SearchCars {

        @Test
        @DisplayName("returns 200 when both location and type are provided and match")
        void returns200WhenBothParamsMatch() throws Exception {
            List<Car> cars = Collections.singletonList(
                    buildCar("C002", "Ford", "Explorer", "SUV", "LosAngeles", 89.99, true, 7));
            when(carRentalService.searchCars("LosAngeles", "SUV")).thenReturn(cars);

            mockMvc.perform(get("/cars/search")
                            .param("location", "LosAngeles")
                            .param("type", "SUV")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is("C002")))
                    .andExpect(jsonPath("$[0].location", is("LosAngeles")))
                    .andExpect(jsonPath("$[0].type", is("SUV")));
        }

        @Test
        @DisplayName("returns 200 when only location is provided")
        void returns200WhenOnlyLocationProvided() throws Exception {
            List<Car> cars = Arrays.asList(
                    buildCar("C001", "Toyota", "Camry", "Sedan", "LosAngeles", 59.99, true, 5),
                    buildCar("C002", "Ford", "Explorer", "SUV", "LosAngeles", 89.99, true, 7),
                    buildCar("C003", "BMW", "5 Series", "Luxury", "LosAngeles", 149.99, true, 5)
            );
            when(carRentalService.searchCars("LosAngeles", null)).thenReturn(cars);

            mockMvc.perform(get("/cars/search")
                            .param("location", "LosAngeles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("returns 200 when only type is provided")
        void returns200WhenOnlyTypeProvided() throws Exception {
            List<Car> cars = Arrays.asList(
                    buildCar("C002", "Ford", "Explorer", "SUV", "LosAngeles", 89.99, true, 7),
                    buildCar("C005", "Chevrolet", "Suburban", "SUV", "NewYork", 99.99, true, 8)
            );
            when(carRentalService.searchCars(null, "SUV")).thenReturn(cars);

            mockMvc.perform(get("/cars/search")
                            .param("type", "SUV"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("returns 200 when neither location nor type is provided")
        void returns200WhenNoParamsProvided() throws Exception {
            List<Car> cars = Arrays.asList(
                    buildCar("C001", "Toyota", "Camry", "Sedan", "LosAngeles", 59.99, true, 5),
                    buildCar("C004", "Honda", "Civic", "Economy", "NewYork", 45.99, true, 5)
            );
            when(carRentalService.searchCars(null, null)).thenReturn(cars);

            mockMvc.perform(get("/cars/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("returns 404 when no cars match")
        void returns404WhenNoCarsMatch() throws Exception {
            when(carRentalService.searchCars(any(), any()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/cars/search")
                            .param("location", "UnknownCity")
                            .param("type", "Hovercraft"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 body is empty when no match")
        void returns404HasNoBody() throws Exception {
            when(carRentalService.searchCars(any(), any()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/cars/search")
                            .param("location", "UnknownCity"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$").doesNotExist());
        }

        @Test
        @DisplayName("passes location and type verbatim to service")
        void passesParamsVerbatimToService() throws Exception {
            when(carRentalService.searchCars("Miami", "Economy")).thenReturn(
                    Collections.singletonList(
                            buildCar("C007", "Toyota", "Corolla", "Economy", "Miami", 49.99, true, 5)));

            mockMvc.perform(get("/cars/search")
                            .param("location", "Miami")
                            .param("type", "Economy"))
                    .andExpect(status().isOk());

            verify(carRentalService).searchCars("Miami", "Economy");
        }

        @Test
        @DisplayName("passes null location to service when param is absent")
        void passesNullLocationToService() throws Exception {
            when(carRentalService.searchCars(isNull(), any())).thenReturn(
                    Collections.singletonList(
                            buildCar("C002", "Ford", "Explorer", "SUV", "LosAngeles", 89.99, true, 7)));

            mockMvc.perform(get("/cars/search")
                            .param("type", "SUV"))
                    .andExpect(status().isOk());

            verify(carRentalService).searchCars(null, "SUV");
        }

        @Test
        @DisplayName("passes null type to service when param is absent")
        void passesNullTypeToService() throws Exception {
            when(carRentalService.searchCars(any(), isNull())).thenReturn(
                    Collections.singletonList(
                            buildCar("C001", "Toyota", "Camry", "Sedan", "LosAngeles", 59.99, true, 5)));

            mockMvc.perform(get("/cars/search")
                            .param("location", "LosAngeles"))
                    .andExpect(status().isOk());

            verify(carRentalService).searchCars("LosAngeles", null);
        }

        @Test
        @DisplayName("passes null for both params when neither is present")
        void passesNullForBothParamsWhenAbsent() throws Exception {
            when(carRentalService.searchCars(isNull(), isNull())).thenReturn(
                    Collections.singletonList(
                            buildCar("C001", "Toyota", "Camry", "Sedan", "LosAngeles", 59.99, true, 5)));

            mockMvc.perform(get("/cars/search"))
                    .andExpect(status().isOk());

            verify(carRentalService).searchCars(null, null);
        }

        @Test
        @DisplayName("response includes pricePerDay and seats fields")
        void responseIncludesPricePerDayAndSeats() throws Exception {
            when(carRentalService.searchCars("Miami", "Economy")).thenReturn(
                    Collections.singletonList(
                            buildCar("C007", "Toyota", "Corolla", "Economy", "Miami", 49.99, true, 5)));

            mockMvc.perform(get("/cars/search")
                            .param("location", "Miami")
                            .param("type", "Economy"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].pricePerDay", is(49.99)))
                    .andExpect(jsonPath("$[0].seats", is(5)));
        }

        @Test
        @DisplayName("returns 404 when matched cars are all unavailable")
        void returns404WhenMatchedCarsUnavailable() throws Exception {
            when(carRentalService.searchCars("Miami", "Luxury"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/cars/search")
                            .param("location", "Miami")
                            .param("type", "Luxury"))
                    .andExpect(status().isNotFound());
        }
    }
}
