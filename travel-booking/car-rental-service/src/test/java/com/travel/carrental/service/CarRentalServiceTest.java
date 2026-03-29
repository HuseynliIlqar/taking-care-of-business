package com.travel.carrental.service;

import com.travel.carrental.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CarRentalService")
class CarRentalServiceTest {

    private CarRentalService carRentalService;

    @BeforeEach
    void setUp() {
        // nhtsaApiEnabled is false by default (field not set by Spring) → uses mock data
        carRentalService = new CarRentalService(new RestTemplate());
    }

    // -------------------------------------------------------------------------
    // getAllCars()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getAllCars()")
    class GetAllCars {

        @Test
        @DisplayName("returns all 26 seeded cars including unavailable ones")
        void returnsAllCars() {
            List<Car> result = carRentalService.getAllCars();

            assertNotNull(result);
            assertEquals(26, result.size());
        }

        @Test
        @DisplayName("returned list is not empty")
        void listIsNotEmpty() {
            assertFalse(carRentalService.getAllCars().isEmpty());
        }

        @Test
        @DisplayName("every car has a non-null id")
        void everyCarHasNonNullId() {
            carRentalService.getAllCars().forEach(c ->
                    assertNotNull(c.getId(), "Car id must not be null"));
        }

        @Test
        @DisplayName("every car has a positive pricePerDay")
        void everyCarHasPositivePrice() {
            carRentalService.getAllCars().forEach(c ->
                    assertTrue(c.getPricePerDay() > 0,
                            "pricePerDay must be positive for car " + c.getId()));
        }

        @Test
        @DisplayName("seeded data contains exactly one unavailable car (C012)")
        void containsOneUnavailableCar() {
            long unavailable = carRentalService.getAllCars().stream()
                    .filter(c -> !c.isAvailable())
                    .count();

            assertEquals(1, unavailable);
        }

        @Test
        @DisplayName("unavailable car is C012 Cadillac Escalade")
        void unavailableCarIsC012() {
            Car unavailable = carRentalService.getAllCars().stream()
                    .filter(c -> !c.isAvailable())
                    .findFirst()
                    .orElseThrow();

            assertEquals("C012", unavailable.getId());
        }
    }

    // -------------------------------------------------------------------------
    // searchCarsByLocation(location)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("searchCarsByLocation(location)")
    class SearchCarsByLocation {

        @Test
        @DisplayName("returns only available cars in matching location")
        void returnsAvailableCarsForLocation() {
            List<Car> result = carRentalService.searchCarsByLocation("LosAngeles");

            assertFalse(result.isEmpty());
            result.forEach(c -> {
                assertEquals("LosAngeles", c.getLocation());
                assertTrue(c.isAvailable());
            });
        }

        @Test
        @DisplayName("LosAngeles returns exactly 4 available cars")
        void losAngelesReturnsFourCars() {
            assertEquals(4, carRentalService.searchCarsByLocation("LosAngeles").size());
        }

        @Test
        @DisplayName("Miami returns exactly 2 available cars (C012 is unavailable)")
        void miamiReturnsTwoCars() {
            // C012 Cadillac is unavailable, so only C007 and C008 are returned
            assertEquals(2, carRentalService.searchCarsByLocation("Miami").size());
        }

        @Test
        @DisplayName("search is case-insensitive")
        void searchIsCaseInsensitive() {
            List<Car> upper = carRentalService.searchCarsByLocation("LosAngeles");
            List<Car> lower = carRentalService.searchCarsByLocation("losangeles");
            List<Car> mixed = carRentalService.searchCarsByLocation("losAngeles");

            assertEquals(upper.size(), lower.size());
            assertEquals(upper.size(), mixed.size());
        }

        @Test
        @DisplayName("returns empty list for unknown location")
        void unknownLocationReturnsEmpty() {
            assertTrue(carRentalService.searchCarsByLocation("UnknownCity").isEmpty());
        }

        @Test
        @DisplayName("returns empty list for empty-string location")
        void emptyStringLocationReturnsEmpty() {
            assertTrue(carRentalService.searchCarsByLocation("").isEmpty());
        }

        @Test
        @DisplayName("NewYork returns exactly 3 available cars")
        void newYorkReturnsThreeCars() {
            assertEquals(3, carRentalService.searchCarsByLocation("NewYork").size());
        }
    }

    // -------------------------------------------------------------------------
    // searchCarsByType(type)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("searchCarsByType(type)")
    class SearchCarsByType {

        @Test
        @DisplayName("returns only available cars of matching type")
        void returnsAvailableCarsForType() {
            List<Car> result = carRentalService.searchCarsByType("SUV");

            assertFalse(result.isEmpty());
            result.forEach(c -> {
                assertEquals("SUV", c.getType());
                assertTrue(c.isAvailable());
            });
        }

        @Test
        @DisplayName("SUV type returns exactly 4 available cars")
        void suvReturnsCorrectCount() {
            // C002 LA, C005 NY, C008 Miami, C011 Chicago — all available
            assertEquals(4, carRentalService.searchCarsByType("SUV").size());
        }

        @Test
        @DisplayName("Economy type returns exactly 8 available cars")
        void economyReturnsThreeCars() {
            // US: C004 NY, C007 Miami, C010 Chicago
            // EU: C013 Prague, C016 London, C019 Paris, C023 Rome, C025 Amsterdam
            assertEquals(8, carRentalService.searchCarsByType("Economy").size());
        }

        @Test
        @DisplayName("Luxury type returns exactly 5 available cars (C012 is unavailable)")
        void luxuryReturnsThreeCars() {
            // US: C003 LA, C006 NY available; C012 Miami NOT available
            // EU: C018 London, C022 Berlin, C024 Rome
            assertEquals(5, carRentalService.searchCarsByType("Luxury").size());
        }

        @Test
        @DisplayName("search is case-insensitive")
        void searchIsCaseInsensitive() {
            List<Car> upper = carRentalService.searchCarsByType("SUV");
            List<Car> lower = carRentalService.searchCarsByType("suv");

            assertEquals(upper.size(), lower.size());
        }

        @Test
        @DisplayName("returns empty list for unknown type")
        void unknownTypeReturnsEmpty() {
            assertTrue(carRentalService.searchCarsByType("Hovercraft").isEmpty());
        }

        @Test
        @DisplayName("returns empty list for empty-string type")
        void emptyStringTypeReturnsEmpty() {
            assertTrue(carRentalService.searchCarsByType("").isEmpty());
        }

        @Test
        @DisplayName("Electric type returns exactly 1 available car")
        void electricReturnsOneCar() {
            List<Car> result = carRentalService.searchCarsByType("Electric");

            assertEquals(1, result.size());
            assertEquals("C009", result.get(0).getId());
        }
    }

    // -------------------------------------------------------------------------
    // searchCars(location, type)  —  both parameters are optional
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("searchCars(location, type)")
    class SearchCars {

        @Test
        @DisplayName("returns all available cars when both params are null")
        void bothNullReturnsAllAvailable() {
            List<Car> result = carRentalService.searchCars(null, null);

            // 26 total, 1 unavailable (C012) → 25 available
            assertEquals(25, result.size());
            result.forEach(c -> assertTrue(c.isAvailable()));
        }

        @Test
        @DisplayName("filters by location when only location is provided")
        void locationOnlyFiltersByLocation() {
            List<Car> result = carRentalService.searchCars("LosAngeles", null);

            assertFalse(result.isEmpty());
            result.forEach(c -> {
                assertEquals("LosAngeles", c.getLocation());
                assertTrue(c.isAvailable());
            });
            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("filters by type when only type is provided")
        void typeOnlyFiltersByType() {
            List<Car> result = carRentalService.searchCars(null, "SUV");

            assertFalse(result.isEmpty());
            result.forEach(c -> {
                assertEquals("SUV", c.getType());
                assertTrue(c.isAvailable());
            });
            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("filters by both location and type when both are provided")
        void bothParamsFilterBothFields() {
            List<Car> result = carRentalService.searchCars("LosAngeles", "SUV");

            assertEquals(1, result.size());
            assertEquals("C002", result.get(0).getId());
            assertEquals("LosAngeles", result.get(0).getLocation());
            assertEquals("SUV", result.get(0).getType());
            assertTrue(result.get(0).isAvailable());
        }

        @Test
        @DisplayName("returns empty list when location has no available cars of given type")
        void noMatchReturnsEmpty() {
            // No Electric cars outside LosAngeles
            List<Car> result = carRentalService.searchCars("NewYork", "Electric");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Miami + Luxury returns empty (only car is unavailable C012)")
        void miamiLuxuryReturnsEmpty() {
            List<Car> result = carRentalService.searchCars("Miami", "Luxury");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("search is case-insensitive for both params")
        void caseInsensitiveForBothParams() {
            List<Car> upper = carRentalService.searchCars("LosAngeles", "SUV");
            List<Car> lower = carRentalService.searchCars("losangeles", "suv");

            assertEquals(upper.size(), lower.size());
        }

        @Test
        @DisplayName("only available cars are returned regardless of null params")
        void onlyAvailableCarsReturned() {
            carRentalService.searchCars(null, null).forEach(c ->
                    assertTrue(c.isAvailable(), "Unavailable car found in results: " + c.getId()));
        }

        @Test
        @DisplayName("LosAngeles + Electric returns exactly 1 car (Tesla Model 3)")
        void losAngelesElectricReturnsTesla() {
            List<Car> result = carRentalService.searchCars("LosAngeles", "Electric");

            assertEquals(1, result.size());
            assertEquals("C009", result.get(0).getId());
        }

        @Test
        @DisplayName("getAllCars() result is independent from searchCars() result")
        void getAllAndSearchReturnIndependentCollections() {
            List<Car> all = carRentalService.getAllCars();
            List<Car> searched = carRentalService.searchCars(null, null);

            assertNotSame(all, searched);
        }
    }
}
