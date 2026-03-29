package com.travel.flight.service;

import com.travel.flight.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FlightService")
class FlightServiceTest {

    private FlightService flightService;

    @BeforeEach
    void setUp() {
        // No credentials configured → service uses mock data fallback
        flightService = new FlightService(new RestTemplate());
    }

    // -------------------------------------------------------------------------
    // getAllFlights()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getAllFlights()")
    class GetAllFlights {

        @Test
        @DisplayName("returns all 8 seeded flights")
        void returnsAllFlights() {
            List<Flight> result = flightService.getAllFlights();

            assertNotNull(result);
            assertEquals(8, result.size());
        }

        @Test
        @DisplayName("returned list is not empty")
        void listIsNotEmpty() {
            assertFalse(flightService.getAllFlights().isEmpty());
        }

        @Test
        @DisplayName("every flight has a non-null id")
        void everyFlightHasNonNullId() {
            flightService.getAllFlights().forEach(f ->
                    assertNotNull(f.getId(), "Flight id must not be null"));
        }

        @Test
        @DisplayName("every flight has a positive price")
        void everyFlightHasPositivePrice() {
            flightService.getAllFlights().forEach(f ->
                    assertTrue(f.getPrice() > 0, "Price must be positive for flight " + f.getId()));
        }

        @Test
        @DisplayName("every flight has a positive availableSeats count")
        void everyFlightHasPositiveAvailableSeats() {
            flightService.getAllFlights().forEach(f ->
                    assertTrue(f.getAvailableSeats() > 0,
                            "availableSeats must be positive for flight " + f.getId()));
        }
    }

    // -------------------------------------------------------------------------
    // searchFlights(origin, destination)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("searchFlights(origin, destination)")
    class SearchFlights {

        @Test
        @DisplayName("returns matching flights for exact-case origin and destination")
        void returnsMatchesForExactCase() {
            List<Flight> result = flightService.searchFlights("NYC", "LAX");

            assertFalse(result.isEmpty());
            result.forEach(f -> {
                assertEquals("NYC", f.getOrigin());
                assertEquals("LAX", f.getDestination());
            });
        }

        @Test
        @DisplayName("NYC -> LAX yields exactly 2 flights")
        void nycToLaxYieldsTwoFlights() {
            List<Flight> result = flightService.searchFlights("NYC", "LAX");

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("search is case-insensitive for origin")
        void caseInsensitiveOrigin() {
            List<Flight> upperCase = flightService.searchFlights("NYC", "LAX");
            List<Flight> lowerCase = flightService.searchFlights("nyc", "lax");

            assertEquals(upperCase.size(), lowerCase.size());
        }

        @Test
        @DisplayName("search is case-insensitive for destination")
        void caseInsensitiveDestination() {
            List<Flight> result = flightService.searchFlights("Nyc", "Lax");

            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when origin matches but destination does not")
        void originMatchesDestinationDoesNot() {
            List<Flight> result = flightService.searchFlights("NYC", "MIA");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when destination matches but origin does not")
        void destinationMatchesOriginDoesNot() {
            List<Flight> result = flightService.searchFlights("MIA", "LAX");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for completely unknown route")
        void unknownRouteReturnsEmpty() {
            List<Flight> result = flightService.searchFlights("ZZZ", "YYY");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for empty-string origin")
        void emptyStringOriginReturnsEmpty() {
            List<Flight> result = flightService.searchFlights("", "LAX");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for empty-string destination")
        void emptyStringDestinationReturnsEmpty() {
            List<Flight> result = flightService.searchFlights("NYC", "");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("LAX -> NYC returns exactly 1 flight")
        void laxToNycReturnsOneResult() {
            List<Flight> result = flightService.searchFlights("LAX", "NYC");

            assertEquals(1, result.size());
            assertEquals("FL003", result.get(0).getId());
        }

        @Test
        @DisplayName("SFO -> ORD returns exactly 1 flight")
        void sfoToOrdReturnsOneResult() {
            List<Flight> result = flightService.searchFlights("SFO", "ORD");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("BOS -> MIA returns exactly 1 flight")
        void bosToMiaReturnsOneResult() {
            List<Flight> result = flightService.searchFlights("BOS", "MIA");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("returned flights carry correct airline names")
        void returnedFlightsHaveCorrectAirlines() {
            List<Flight> result = flightService.searchFlights("NYC", "LAX");

            List<String> airlines = List.of("Delta Airlines", "United Airlines");
            result.forEach(f ->
                    assertTrue(airlines.contains(f.getAirline()),
                            "Unexpected airline: " + f.getAirline()));
        }

        @Test
        @DisplayName("getAllFlights() result is independent from searchFlights() result")
        void getAllAndSearchReturnIndependentCollections() {
            List<Flight> all = flightService.getAllFlights();
            List<Flight> searched = flightService.searchFlights("NYC", "LAX");

            // Mutating one list must not affect the other
            assertNotSame(all, searched);
        }
    }
}
