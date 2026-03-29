package com.travel.hotel.service;

import com.travel.hotel.model.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HotelService")
class HotelServiceTest {

    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        // No credentials configured → service uses mock data fallback
        hotelService = new HotelService(new RestTemplate());
    }

    // -------------------------------------------------------------------------
    // getAllHotels()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getAllHotels()")
    class GetAllHotels {

        @Test
        @DisplayName("returns all 14 seeded hotels")
        void returnsAllHotels() {
            List<Hotel> result = hotelService.getAllHotels();

            assertNotNull(result);
            assertEquals(14, result.size());
        }

        @Test
        @DisplayName("returned list is not empty")
        void listIsNotEmpty() {
            assertFalse(hotelService.getAllHotels().isEmpty());
        }

        @Test
        @DisplayName("every hotel has a non-null id")
        void everyHotelHasNonNullId() {
            hotelService.getAllHotels().forEach(h ->
                    assertNotNull(h.getId(), "Hotel id must not be null"));
        }

        @Test
        @DisplayName("every hotel has a positive pricePerNight")
        void everyHotelHasPositivePrice() {
            hotelService.getAllHotels().forEach(h ->
                    assertTrue(h.getPricePerNight() > 0,
                            "pricePerNight must be positive for hotel " + h.getId()));
        }

        @Test
        @DisplayName("every hotel has a rating between 1.0 and 5.0")
        void everyHotelHasValidRating() {
            hotelService.getAllHotels().forEach(h -> {
                assertTrue(h.getRating() >= 1.0 && h.getRating() <= 5.0,
                        "Rating out of range for hotel " + h.getId());
            });
        }

        @Test
        @DisplayName("every hotel has a non-null name")
        void everyHotelHasNonNullName() {
            hotelService.getAllHotels().forEach(h ->
                    assertNotNull(h.getName(), "Hotel name must not be null"));
        }
    }

    // -------------------------------------------------------------------------
    // searchHotels(city)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("searchHotels(city)")
    class SearchHotels {

        @Test
        @DisplayName("returns matching hotels for exact-case city")
        void returnsMatchesForExactCase() {
            List<Hotel> result = hotelService.searchHotels("NewYork");

            assertFalse(result.isEmpty());
            result.forEach(h -> assertEquals("NewYork", h.getCity()));
        }

        @Test
        @DisplayName("NewYork search yields exactly 2 hotels")
        void newYorkYieldsTwoHotels() {
            assertEquals(2, hotelService.searchHotels("NewYork").size());
        }

        @Test
        @DisplayName("LosAngeles search yields exactly 2 hotels")
        void losAngelesYieldsTwoHotels() {
            assertEquals(2, hotelService.searchHotels("LosAngeles").size());
        }

        @Test
        @DisplayName("search is case-insensitive")
        void searchIsCaseInsensitive() {
            List<Hotel> upperResult = hotelService.searchHotels("NewYork");
            List<Hotel> lowerResult = hotelService.searchHotels("newyork");
            List<Hotel> mixedResult = hotelService.searchHotels("newYORK");

            assertEquals(upperResult.size(), lowerResult.size());
            assertEquals(upperResult.size(), mixedResult.size());
        }

        @Test
        @DisplayName("returns empty list for unknown city")
        void unknownCityReturnsEmpty() {
            List<Hotel> result = hotelService.searchHotels("UnknownCity");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for empty-string city")
        void emptyStringCityReturnsEmpty() {
            List<Hotel> result = hotelService.searchHotels("");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Miami search yields exactly 1 hotel")
        void miamiYieldsOneHotel() {
            List<Hotel> result = hotelService.searchHotels("Miami");

            assertEquals(1, result.size());
            assertEquals("H005", result.get(0).getId());
        }

        @Test
        @DisplayName("Chicago search yields exactly 1 hotel")
        void chicagoYieldsOneHotel() {
            assertEquals(1, hotelService.searchHotels("Chicago").size());
        }

        @Test
        @DisplayName("SanFrancisco search yields exactly 1 hotel")
        void sanFranciscoYieldsOneHotel() {
            assertEquals(1, hotelService.searchHotels("SanFrancisco").size());
        }

        @Test
        @DisplayName("Boston search yields exactly 1 hotel")
        void bostonYieldsOneHotel() {
            assertEquals(1, hotelService.searchHotels("Boston").size());
        }

        @Test
        @DisplayName("returned hotels have non-null amenities")
        void returnedHotelsHaveAmenities() {
            hotelService.searchHotels("NewYork").forEach(h ->
                    assertNotNull(h.getAmenities(), "Amenities must not be null"));
        }

        @Test
        @DisplayName("getAllHotels() result is independent from searchHotels() result")
        void getAllAndSearchReturnIndependentCollections() {
            List<Hotel> all = hotelService.getAllHotels();
            List<Hotel> searched = hotelService.searchHotels("NewYork");

            assertNotSame(all, searched);
        }
    }
}
