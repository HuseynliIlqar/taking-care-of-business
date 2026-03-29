package com.travel.flight.controller;

import com.travel.flight.model.Flight;
import com.travel.flight.service.FlightService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@DisplayName("FlightController")
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @BeforeEach
    void resetMocks() {
        reset(flightService);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Flight buildFlight(String id, String airline, String origin, String destination,
                               String flightNumber, double price, int seats) {
        return new Flight(id, airline, origin, destination,
                "2024-06-01T08:00:00", "2024-06-01T11:30:00",
                flightNumber, price, seats);
    }

    // -------------------------------------------------------------------------
    // GET /flights
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /flights")
    class GetAllFlights {

        @Test
        @DisplayName("returns 200 and JSON array when flights exist")
        void returns200WithFlightList() throws Exception {
            List<Flight> flights = Arrays.asList(
                    buildFlight("FL001", "Delta Airlines", "NYC", "LAX", "DL101", 299.99, 45),
                    buildFlight("FL002", "United Airlines", "NYC", "LAX", "UA205", 349.99, 30)
            );
            when(flightService.getAllFlights()).thenReturn(flights);

            mockMvc.perform(get("/flights").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is("FL001")))
                    .andExpect(jsonPath("$[0].airline", is("Delta Airlines")))
                    .andExpect(jsonPath("$[0].origin", is("NYC")))
                    .andExpect(jsonPath("$[0].destination", is("LAX")))
                    .andExpect(jsonPath("$[0].price", is(299.99)))
                    .andExpect(jsonPath("$[0].availableSeats", is(45)))
                    .andExpect(jsonPath("$[1].id", is("FL002")));
        }

        @Test
        @DisplayName("returns 200 with empty array when no flights exist")
        void returns200WithEmptyList() throws Exception {
            when(flightService.getAllFlights()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/flights").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("returns 200 with single-element array")
        void returns200WithSingleFlight() throws Exception {
            when(flightService.getAllFlights()).thenReturn(
                    Collections.singletonList(
                            buildFlight("FL001", "Delta Airlines", "NYC", "LAX", "DL101", 299.99, 45)));

            mockMvc.perform(get("/flights"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("delegates to FlightService exactly once")
        void delegatesToServiceOnce() throws Exception {
            when(flightService.getAllFlights()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/flights"));

            verify(flightService, times(1)).getAllFlights();
            verifyNoMoreInteractions(flightService);
        }

        @Test
        @DisplayName("serialises flightNumber field in response")
        void serialisesFlightNumber() throws Exception {
            when(flightService.getAllFlights()).thenReturn(
                    Collections.singletonList(
                            buildFlight("FL001", "Delta Airlines", "NYC", "LAX", "DL101", 299.99, 45)));

            mockMvc.perform(get("/flights"))
                    .andExpect(jsonPath("$[0].flightNumber", is("DL101")));
        }
    }

    // -------------------------------------------------------------------------
    // GET /flights/search
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /flights/search")
    class SearchFlights {

        @Test
        @DisplayName("returns 200 and matching flights when route is found")
        void returns200WhenFlightsFound() throws Exception {
            List<Flight> flights = Collections.singletonList(
                    buildFlight("FL001", "Delta Airlines", "NYC", "LAX", "DL101", 299.99, 45));
            when(flightService.searchFlights("NYC", "LAX")).thenReturn(flights);

            mockMvc.perform(get("/flights/search")
                            .param("origin", "NYC")
                            .param("destination", "LAX")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].origin", is("NYC")))
                    .andExpect(jsonPath("$[0].destination", is("LAX")));
        }

        @Test
        @DisplayName("returns 404 when no flights match the route")
        void returns404WhenNoFlightsFound() throws Exception {
            when(flightService.searchFlights(anyString(), anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/flights/search")
                            .param("origin", "ZZZ")
                            .param("destination", "YYY"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 body is empty (no JSON body on 404)")
        void returns404HasNoBody() throws Exception {
            when(flightService.searchFlights(anyString(), anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/flights/search")
                            .param("origin", "ZZZ")
                            .param("destination", "YYY"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$").doesNotExist());
        }

        @Test
        @DisplayName("passes origin and destination verbatim to service")
        void passesParamsVerbatimToService() throws Exception {
            when(flightService.searchFlights("SFO", "ORD"))
                    .thenReturn(Collections.singletonList(
                            buildFlight("FL004", "Southwest Airlines", "SFO", "ORD", "SW415", 199.99, 80)));

            mockMvc.perform(get("/flights/search")
                            .param("origin", "SFO")
                            .param("destination", "ORD"))
                    .andExpect(status().isOk());

            verify(flightService).searchFlights("SFO", "ORD");
        }

        @Test
        @DisplayName("returns 400 when origin parameter is missing")
        void returns400WhenOriginMissing() throws Exception {
            mockMvc.perform(get("/flights/search")
                            .param("destination", "LAX"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when destination parameter is missing")
        void returns400WhenDestinationMissing() throws Exception {
            mockMvc.perform(get("/flights/search")
                            .param("origin", "NYC"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when both parameters are missing")
        void returns400WhenBothParamsMissing() throws Exception {
            mockMvc.perform(get("/flights/search"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns multiple matching flights in array")
        void returnsMultipleMatchingFlights() throws Exception {
            List<Flight> flights = Arrays.asList(
                    buildFlight("FL001", "Delta Airlines", "NYC", "LAX", "DL101", 299.99, 45),
                    buildFlight("FL002", "United Airlines", "NYC", "LAX", "UA205", 349.99, 30)
            );
            when(flightService.searchFlights("NYC", "LAX")).thenReturn(flights);

            mockMvc.perform(get("/flights/search")
                            .param("origin", "NYC")
                            .param("destination", "LAX"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("response includes price and availableSeats fields")
        void responseIncludesPriceAndSeats() throws Exception {
            when(flightService.searchFlights("BOS", "MIA")).thenReturn(
                    Collections.singletonList(
                            buildFlight("FL005", "JetBlue Airways", "BOS", "MIA", "JB520", 249.99, 55)));

            mockMvc.perform(get("/flights/search")
                            .param("origin", "BOS")
                            .param("destination", "MIA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].price", is(249.99)))
                    .andExpect(jsonPath("$[0].availableSeats", is(55)));
        }
    }
}
