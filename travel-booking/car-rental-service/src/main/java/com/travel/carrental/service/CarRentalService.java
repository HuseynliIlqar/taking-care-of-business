package com.travel.carrental.service;

import com.travel.carrental.model.Car;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CarRentalService {

    private static final Logger log = LoggerFactory.getLogger(CarRentalService.class);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SEARCH_URL =
            "https://booking-com.p.rapidapi.com/v1/car-rental/search"
            + "?pick_up_latitude={lat}&pick_up_longitude={lng}"
            + "&drop_off_latitude={lat}&drop_off_longitude={lng}"
            + "&pick_up_datetime={pickUp}&drop_off_datetime={dropOff}"
            + "&sort_by=recommended&from_country={country}&locale=en-gb&currency=USD";

    // City name (lowercase) → [latitude, longitude]
    private static final Map<String, double[]> CITY_COORDS = Map.ofEntries(
            Map.entry("losangeles",   new double[]{ 34.0522,  -118.2437}),
            Map.entry("newyork",      new double[]{ 40.7128,   -74.0060}),
            Map.entry("miami",        new double[]{ 25.7617,   -80.1918}),
            Map.entry("chicago",      new double[]{ 41.8781,   -87.6298}),
            Map.entry("sanfrancisco", new double[]{ 37.7749,  -122.4194}),
            Map.entry("boston",       new double[]{ 42.3601,   -71.0589}),
            Map.entry("prague",       new double[]{ 50.0880,    14.4208}),
            Map.entry("london",       new double[]{ 51.5074,    -0.1278}),
            Map.entry("paris",        new double[]{ 48.8566,     2.3522}),
            Map.entry("berlin",       new double[]{ 52.5200,    13.4050}),
            Map.entry("rome",         new double[]{ 41.9028,    12.4964}),
            Map.entry("amsterdam",    new double[]{ 52.3676,     4.9041})
    );

    // City name (lowercase) → from_country code required by Booking.com API
    private static final Map<String, String> CITY_COUNTRY = Map.ofEntries(
            Map.entry("losangeles",   "it"),
            Map.entry("newyork",      "it"),
            Map.entry("miami",        "it"),
            Map.entry("chicago",      "it"),
            Map.entry("sanfrancisco", "it"),
            Map.entry("boston",       "it"),
            Map.entry("prague",       "cs"),
            Map.entry("london",       "de"),
            Map.entry("paris",        "fr"),
            Map.entry("berlin",       "de"),
            Map.entry("rome",         "it"),
            Map.entry("amsterdam",    "nl")
    );

    @Value("${rapidapi.key:}")
    private String apiKey;

    @Value("${rapidapi.host:booking-com.p.rapidapi.com}")
    private String apiHost;

    private final RestTemplate restTemplate;

    public CarRentalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Car> getAllCars() {
        return getMockCars();
    }

    public List<Car> searchCarsByLocation(String location) {
        if (!apiKeyConfigured()) {
            return getMockCars().stream()
                    .filter(c -> c.getLocation().equalsIgnoreCase(location) && c.isAvailable())
                    .collect(Collectors.toList());
        }
        try {
            List<Car> cars = fetchFromBooking(location, null);
            if (!cars.isEmpty()) return cars;
        } catch (Exception e) {
            log.warn("Booking.com car rental API failed, using mock data: {}", e.getMessage());
        }
        return fallbackMockCars(location, null);
    }

    public List<Car> searchCarsByType(String type) {
        return getMockCars().stream()
                .filter(c -> c.getType().equalsIgnoreCase(type) && c.isAvailable())
                .collect(Collectors.toList());
    }

    public List<Car> searchCars(String location, String type) {
        if (apiKeyConfigured() && location != null && !location.isEmpty()) {
            try {
                List<Car> cars = fetchFromBooking(location, type);
                if (!cars.isEmpty()) return cars;
            } catch (Exception e) {
                log.warn("Booking.com car rental API failed, using mock data: {}", e.getMessage());
            }
            // API key configured but call failed or returned empty — fallback with broader mock
            return fallbackMockCars(location, type);
        }
        return getMockCars().stream()
                .filter(Car::isAvailable)
                .filter(c -> location == null || c.getLocation().equalsIgnoreCase(location))
                .filter(c -> type == null || c.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    private List<Car> fallbackMockCars(String location, String type) {
        List<Car> filtered = getMockCars().stream()
                .filter(Car::isAvailable)
                .filter(c -> location == null || c.getLocation().equalsIgnoreCase(location))
                .filter(c -> type == null || c.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        return filtered.isEmpty() ? getMockCars().stream().filter(Car::isAvailable).collect(Collectors.toList()) : filtered;
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
    private List<Car> fetchFromBooking(String location, String type) {
        double[] coords = CITY_COORDS.get(location.toLowerCase());
        if (coords == null) {
            log.debug("No coordinates for '{}', using mock data", location);
            return Collections.emptyList();
        }

        String country = CITY_COUNTRY.getOrDefault(location.toLowerCase(), "it");
        LocalDateTime pickUp  = LocalDateTime.now().plusDays(7)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime dropOff = pickUp.plusDays(1);

        ResponseEntity<Map> response = restTemplate.exchange(
                SEARCH_URL, HttpMethod.GET,
                new HttpEntity<>(buildHeaders()), Map.class,
                coords[0], coords[1],
                pickUp.format(DT_FMT), dropOff.format(DT_FMT),
                country);

        Map<String, Object> body = response.getBody();
        if (body == null) return Collections.emptyList();

        List<Map<String, Object>> vehicles =
                (List<Map<String, Object>>) body.get("search_results");
        if (vehicles == null || vehicles.isEmpty()) return Collections.emptyList();

        List<Car> result = new ArrayList<>();
        for (int i = 0; i < vehicles.size(); i++) {
            Car c = mapCar(vehicles.get(i), location, i + 1);
            if (c == null) continue;
            if (type != null && !type.isEmpty() && !c.getType().equalsIgnoreCase(type)) continue;
            result.add(c);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Car mapCar(Map<String, Object> raw, String location, int index) {
        try {
            Map<String, Object> vi = (Map<String, Object>) raw.get("vehicle_info");
            Map<String, Object> si = (Map<String, Object>) raw.get("supplier_info");
            Map<String, Object> pi = (Map<String, Object>) raw.get("pricing_info");
            if (vi == null) vi = Collections.emptyMap();

            String id    = "BK-C-" + index;
            String model = getString(vi, "v_name");
            if (model.isEmpty()) model = "Car " + index;

            String make  = si != null ? getString(si, "name") : "";
            if (make.isEmpty()) make = "Rental";

            String type  = getString(vi, "group");
            if (type.isEmpty()) type = "Economy";

            int seats = 5;
            Object seatsObj = vi.get("seats");
            if (seatsObj != null) {
                try { seats = Integer.parseInt(seatsObj.toString()); }
                catch (NumberFormatException ignored) {}
            }

            double price = 59.99;
            if (pi != null && pi.get("base_price") != null) {
                price = Double.parseDouble(pi.get("base_price").toString());
            }

            return new Car(id, make, model, type, location, price, true, seats);
        } catch (Exception e) {
            log.warn("Could not map car entry #{}: {}", index, e.getMessage());
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private List<Car> getMockCars() {
        return Arrays.asList(
                // US cities
                new Car("C001", "Toyota",        "Camry",         "Sedan",    "LosAngeles",  59.99,  true,  5),
                new Car("C002", "Ford",          "Explorer",      "SUV",      "LosAngeles",  89.99,  true,  7),
                new Car("C003", "BMW",           "5 Series",      "Luxury",   "LosAngeles",  149.99, true,  5),
                new Car("C004", "Honda",         "Civic",         "Economy",  "NewYork",     45.99,  true,  5),
                new Car("C005", "Chevrolet",     "Suburban",      "SUV",      "NewYork",     99.99,  true,  8),
                new Car("C006", "Mercedes-Benz", "E-Class",       "Luxury",   "NewYork",     179.99, true,  5),
                new Car("C007", "Toyota",        "Corolla",       "Economy",  "Miami",       49.99,  true,  5),
                new Car("C008", "Jeep",          "Grand Cherokee","SUV",      "Miami",       94.99,  true,  5),
                new Car("C009", "Tesla",         "Model 3",       "Electric", "LosAngeles",  119.99, true,  5),
                new Car("C010", "Hyundai",       "Elantra",       "Economy",  "Chicago",     42.99,  true,  5),
                new Car("C011", "Nissan",        "Pathfinder",    "SUV",      "Chicago",     84.99,  true,  6),
                new Car("C012", "Cadillac",      "Escalade",      "Luxury",   "Miami",       199.99, false, 7),
                // European cities
                new Car("C013", "Skoda",         "Fabia",         "Economy",  "Prague",      35.99,  true,  5),
                new Car("C014", "Volkswagen",    "Golf",          "Compact",  "Prague",      49.99,  true,  5),
                new Car("C015", "Skoda",         "Octavia",       "Compact",  "Prague",      55.99,  true,  5),
                new Car("C016", "Vauxhall",      "Corsa",         "Economy",  "London",      45.99,  true,  5),
                new Car("C017", "Ford",          "Focus",         "Compact",  "London",      59.99,  true,  5),
                new Car("C018", "BMW",           "3 Series",      "Luxury",   "London",      139.99, true,  5),
                new Car("C019", "Renault",       "Clio",          "Economy",  "Paris",       38.99,  true,  5),
                new Car("C020", "Peugeot",       "308",           "Compact",  "Paris",       52.99,  true,  5),
                new Car("C021", "Volkswagen",    "Passat",        "Sedan",    "Berlin",      54.99,  true,  5),
                new Car("C022", "Audi",          "A4",            "Luxury",   "Berlin",      129.99, true,  5),
                new Car("C023", "Fiat",          "500",           "Economy",  "Rome",        36.99,  true,  4),
                new Car("C024", "Alfa Romeo",    "Giulia",        "Luxury",   "Rome",        119.99, true,  5),
                new Car("C025", "Volkswagen",    "Polo",          "Economy",  "Amsterdam",   39.99,  true,  5),
                new Car("C026", "Mercedes-Benz", "A-Class",       "Compact",  "Amsterdam",   69.99,  true,  5)
        );
    }
}
