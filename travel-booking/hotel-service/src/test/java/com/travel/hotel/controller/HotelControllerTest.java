package com.travel.hotel.controller;

import com.travel.hotel.model.Hotel;
import com.travel.hotel.service.HotelService;
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

@WebMvcTest(HotelController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@DisplayName("HotelController")
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @BeforeEach
    void resetMocks() {
        reset(hotelService);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Hotel buildHotel(String id, String name, String city, double rating,
                              double pricePerNight, int availableRooms) {
        return new Hotel(id, name, city, "123 Test St",
                rating, pricePerNight, availableRooms, "WiFi, Pool");
    }

    // -------------------------------------------------------------------------
    // GET /hotels
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /hotels")
    class GetAllHotels {

        @Test
        @DisplayName("returns 200 and JSON array when hotels exist")
        void returns200WithHotelList() throws Exception {
            List<Hotel> hotels = Arrays.asList(
                    buildHotel("H001", "The Grand Plaza", "LosAngeles", 4.5, 289.99, 15),
                    buildHotel("H003", "Manhattan Luxury Hotel", "NewYork", 4.8, 499.99, 8)
            );
            when(hotelService.getAllHotels()).thenReturn(hotels);

            mockMvc.perform(get("/hotels").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is("H001")))
                    .andExpect(jsonPath("$[0].name", is("The Grand Plaza")))
                    .andExpect(jsonPath("$[0].city", is("LosAngeles")))
                    .andExpect(jsonPath("$[0].rating", is(4.5)))
                    .andExpect(jsonPath("$[0].pricePerNight", is(289.99)))
                    .andExpect(jsonPath("$[0].availableRooms", is(15)))
                    .andExpect(jsonPath("$[1].id", is("H003")));
        }

        @Test
        @DisplayName("returns 200 with empty array when no hotels exist")
        void returns200WithEmptyList() throws Exception {
            when(hotelService.getAllHotels()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/hotels").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("returns 200 with single-element array")
        void returns200WithSingleHotel() throws Exception {
            when(hotelService.getAllHotels()).thenReturn(
                    Collections.singletonList(
                            buildHotel("H001", "The Grand Plaza", "LosAngeles", 4.5, 289.99, 15)));

            mockMvc.perform(get("/hotels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("delegates to HotelService exactly once")
        void delegatesToServiceOnce() throws Exception {
            when(hotelService.getAllHotels()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/hotels"));

            verify(hotelService, times(1)).getAllHotels();
            verifyNoMoreInteractions(hotelService);
        }

        @Test
        @DisplayName("serialises amenities field in response")
        void serialisesAmenities() throws Exception {
            Hotel hotel = new Hotel("H001", "The Grand Plaza", "LosAngeles",
                    "123 Sunset Blvd", 4.5, 289.99, 15, "Pool, Spa, Gym");
            when(hotelService.getAllHotels()).thenReturn(Collections.singletonList(hotel));

            mockMvc.perform(get("/hotels"))
                    .andExpect(jsonPath("$[0].amenities", is("Pool, Spa, Gym")));
        }
    }

    // -------------------------------------------------------------------------
    // GET /hotels/search
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /hotels/search")
    class SearchHotels {

        @Test
        @DisplayName("returns 200 and matching hotels when city is found")
        void returns200WhenHotelsFound() throws Exception {
            List<Hotel> hotels = Arrays.asList(
                    buildHotel("H003", "Manhattan Luxury Hotel", "NewYork", 4.8, 499.99, 8),
                    buildHotel("H004", "Times Square Budget Inn", "NewYork", 3.8, 149.99, 30)
            );
            when(hotelService.searchHotels("NewYork")).thenReturn(hotels);

            mockMvc.perform(get("/hotels/search")
                            .param("location", "NewYork")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].city", is("NewYork")))
                    .andExpect(jsonPath("$[1].city", is("NewYork")));
        }

        @Test
        @DisplayName("returns 404 when no hotels match the city")
        void returns404WhenNoHotelsFound() throws Exception {
            when(hotelService.searchHotels(anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/hotels/search")
                            .param("location", "UnknownCity"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 body is empty (no JSON body on 404)")
        void returns404HasNoBody() throws Exception {
            when(hotelService.searchHotels(anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/hotels/search")
                            .param("location", "UnknownCity"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$").doesNotExist());
        }

        @Test
        @DisplayName("passes location parameter verbatim to service")
        void passesLocationVerbatimToService() throws Exception {
            when(hotelService.searchHotels("Miami")).thenReturn(
                    Collections.singletonList(
                            buildHotel("H005", "Miami Beach Resort", "Miami", 4.6, 349.99, 12)));

            mockMvc.perform(get("/hotels/search")
                            .param("location", "Miami"))
                    .andExpect(status().isOk());

            verify(hotelService).searchHotels("Miami");
        }

        @Test
        @DisplayName("returns 400 when location parameter is missing")
        void returns400WhenLocationMissing() throws Exception {
            mockMvc.perform(get("/hotels/search"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns single hotel in array when only one matches")
        void returnsSingleMatchInArray() throws Exception {
            when(hotelService.searchHotels("Miami")).thenReturn(
                    Collections.singletonList(
                            buildHotel("H005", "Miami Beach Resort", "Miami", 4.6, 349.99, 12)));

            mockMvc.perform(get("/hotels/search")
                            .param("location", "Miami"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is("H005")));
        }

        @Test
        @DisplayName("response includes rating and pricePerNight fields")
        void responseIncludesRatingAndPrice() throws Exception {
            when(hotelService.searchHotels("Chicago")).thenReturn(
                    Collections.singletonList(
                            buildHotel("H006", "Chicago Downtown Suites", "Chicago", 4.3, 229.99, 18)));

            mockMvc.perform(get("/hotels/search")
                            .param("location", "Chicago"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].rating", is(4.3)))
                    .andExpect(jsonPath("$[0].pricePerNight", is(229.99)));
        }

        @Test
        @DisplayName("search with LosAngeles returns both LA hotels")
        void losAngelesReturnsMultipleHotels() throws Exception {
            List<Hotel> laHotels = Arrays.asList(
                    buildHotel("H001", "The Grand Plaza", "LosAngeles", 4.5, 289.99, 15),
                    buildHotel("H002", "Pacific Coast Inn", "LosAngeles", 4.2, 199.99, 22)
            );
            when(hotelService.searchHotels("LosAngeles")).thenReturn(laHotels);

            mockMvc.perform(get("/hotels/search")
                            .param("location", "LosAngeles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }
}
