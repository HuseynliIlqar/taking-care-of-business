package com.travel.hotel.service;

import com.travel.hotel.model.Hotel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelService.class);

    private static final String LOCATION_URL =
            "https://booking-com.p.rapidapi.com/v1/hotels/locations"
            + "?name={city}&locale=en-gb";

    private static final String SEARCH_URL =
            "https://booking-com.p.rapidapi.com/v1/hotels/search"
            + "?dest_id={destId}&dest_type=city"
            + "&checkin_date={checkIn}&checkout_date={checkOut}"
            + "&adults_number=2&room_number=1"
            + "&order_by=popularity&locale=en-gb&currency=USD&page_number=0";

    @Value("${rapidapi.key:}")
    private String apiKey;

    @Value("${rapidapi.host:booking-com.p.rapidapi.com}")
    private String apiHost;

    private final RestTemplate restTemplate;

    public HotelService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Hotel> getAllHotels() {
        return getMockHotels();
    }

    public List<Hotel> searchHotels(String city) {
        if (city == null || city.isEmpty()) return Collections.emptyList();

        if (!apiKeyConfigured()) {
            return getMockHotels().stream()
                    .filter(h -> h.getCity().equalsIgnoreCase(city))
                    .collect(Collectors.toList());
        }
        try {
            String destId = fetchDestId(city);
            if (destId == null) {
                return fallbackMockHotels(city);
            }
            return fetchHotels(destId, city);
        } catch (Exception e) {
            log.warn("Booking.com hotel API failed, using mock data: {}", e.getMessage());
            return fallbackMockHotels(city);
        }
    }

    private List<Hotel> fallbackMockHotels(String city) {
        List<Hotel> filtered = getMockHotels().stream()
                .filter(h -> h.getCity().equalsIgnoreCase(city))
                .collect(Collectors.toList());
        return filtered.isEmpty() ? getMockHotels() : filtered;
    }

    private boolean apiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", apiHost);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @SuppressWarnings("unchecked")
    private String fetchDestId(String city) {
        ResponseEntity<List> response = restTemplate.exchange(
                LOCATION_URL, HttpMethod.GET,
                new HttpEntity<>(buildHeaders()), List.class, city);

        List<Map<String, Object>> locations = response.getBody();
        if (locations == null || locations.isEmpty()) return null;

        for (Map<String, Object> loc : locations) {
            if ("city".equals(loc.get("dest_type"))) {
                Object id = loc.get("dest_id");
                return id != null ? id.toString() : null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Hotel> fetchHotels(String destId, String cityName) {
        String checkIn  = LocalDate.now().plusDays(7).toString();
        String checkOut = LocalDate.now().plusDays(8).toString();

        ResponseEntity<Map> response = restTemplate.exchange(
                SEARCH_URL, HttpMethod.GET,
                new HttpEntity<>(buildHeaders()), Map.class,
                destId, checkIn, checkOut);

        Map<String, Object> body = response.getBody();
        if (body == null) return Collections.emptyList();

        List<Map<String, Object>> results =
                (List<Map<String, Object>>) body.get("result");
        if (results == null) return Collections.emptyList();

        List<Hotel> hotels = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Hotel h = mapHotel(results.get(i), cityName, i + 1);
            if (h != null) hotels.add(h);
        }
        return hotels;
    }

    @SuppressWarnings("unchecked")
    private Hotel mapHotel(Map<String, Object> raw, String cityName, int index) {
        try {
            String id       = getString(raw, "hotel_id");
            if (id.isEmpty()) id = "BK-H-" + index;

            String name     = getString(raw, "hotel_name");
            if (name.isEmpty()) name = "Hotel " + index;

            String address  = getString(raw, "address");
            double rating   = toDouble(raw.get("review_score"));
            double price    = toDouble(raw.get("min_total_price"));
            int    rooms    = 10;

            Object roomsObj = raw.get("available_rooms");
            if (roomsObj != null) rooms = ((Number) roomsObj).intValue();

            String amenities = getString(raw, "hotel_facilities");
            if (amenities.isEmpty()) amenities = "WiFi, Parking";

            // rating from Booking.com is 0-10, normalize to 0-5
            if (rating > 5.0) rating = rating / 2.0;

            return new Hotel(id, name, cityName, address,
                    rating, price, rooms, amenities);
        } catch (Exception e) {
            log.warn("Could not map hotel entry #{}: {}", index, e.getMessage());
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private List<Hotel> getMockHotels() {
        return Arrays.asList(
                new Hotel("H001", "The Grand Plaza", "LosAngeles",
                        "123 Sunset Blvd, Los Angeles, CA 90028",
                        4.5, 289.99, 15, "Pool, Spa, Gym, Restaurant"),
                new Hotel("H002", "Pacific Coast Inn", "LosAngeles",
                        "456 Ocean Ave, Santa Monica, CA 90401",
                        4.2, 199.99, 22, "Beach Access, Breakfast, Parking"),
                new Hotel("H003", "Manhattan Luxury Hotel", "NewYork",
                        "789 5th Avenue, New York, NY 10022",
                        4.8, 499.99, 8, "Rooftop Bar, Concierge, Gym, Spa"),
                new Hotel("H004", "Times Square Budget Inn", "NewYork",
                        "321 W 42nd St, New York, NY 10036",
                        3.8, 149.99, 30, "WiFi, Breakfast, City View"),
                new Hotel("H005", "Miami Beach Resort", "Miami",
                        "1 Ocean Drive, Miami Beach, FL 33139",
                        4.6, 349.99, 12, "Private Beach, Pool, Water Sports, Bar"),
                new Hotel("H006", "Chicago Downtown Suites", "Chicago",
                        "500 N Michigan Ave, Chicago, IL 60611",
                        4.3, 229.99, 18, "Gym, Restaurant, Business Center"),
                new Hotel("H007", "Golden Gate Hotel", "SanFrancisco",
                        "250 Market St, San Francisco, CA 94105",
                        4.1, 279.99, 20, "Bay View, Gym, Restaurant"),
                new Hotel("H008", "Boston Harbor Inn", "Boston",
                        "70 Rowes Wharf, Boston, MA 02110",
                        4.4, 319.99, 10, "Harbor View, Pool, Spa, Fine Dining"),
                new Hotel("H009", "Prague Castle View", "Prague",
                        "Malostranske Nam. 2, Prague 118 00",
                        4.5, 179.99, 14, "Castle View, Spa, Restaurant, WiFi"),
                new Hotel("H010", "London Bridge Hotel", "London",
                        "8-18 London Bridge St, London SE1 9SG",
                        4.3, 289.99, 20, "River View, Gym, Bar, Breakfast"),
                new Hotel("H011", "Paris Champs-Elysees Boutique", "Paris",
                        "15 Av. des Champs-Elysees, 75008 Paris",
                        4.7, 399.99, 9, "City View, Michelin Restaurant, Concierge"),
                new Hotel("H012", "Berlin Mitte Apartments", "Berlin",
                        "Unter den Linden 5, 10117 Berlin",
                        4.1, 159.99, 25, "Kitchenette, Gym, Parking, WiFi"),
                new Hotel("H013", "Hotel Roma Colosseo", "Rome",
                        "Via Labicana 144, 00184 Roma RM",
                        4.4, 219.99, 16, "Colosseum View, Pool, Restaurant, Spa"),
                new Hotel("H014", "Amsterdam Canal Suite", "Amsterdam",
                        "Keizersgracht 384, 1016 GB Amsterdam",
                        4.6, 259.99, 11, "Canal View, Bicycle Rental, Bar, Breakfast")
        );
    }
}
