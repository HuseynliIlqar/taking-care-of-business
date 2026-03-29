package com.travel.flight.service;

import com.travel.flight.model.Flight;
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
public class FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightService.class);

    private static final String SEARCH_URL =
            "https://booking-com.p.rapidapi.com/v1/flights/search"
            + "?from_code={from}&to_code={to}&depart_date={date}"
            + "&adults=1&cabin_class=ECONOMY&flight_type=ONEWAY"
            + "&locale=en-gb&currency=USD&page_number=0";

    @Value("${rapidapi.key:}")
    private String apiKey;

    @Value("${rapidapi.host:booking-com.p.rapidapi.com}")
    private String apiHost;

    private final RestTemplate restTemplate;

    public FlightService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Flight> getAllFlights() {
        return getMockFlights();
    }

    public List<Flight> searchFlights(String origin, String destination) {
        if (!apiKeyConfigured()) {
            return getMockFlights().stream()
                    .filter(f -> f.getOrigin().equalsIgnoreCase(origin)
                            && f.getDestination().equalsIgnoreCase(destination))
                    .collect(Collectors.toList());
        }
        try {
            return fetchFromBooking(origin, destination);
        } catch (Exception e) {
            log.warn("Booking.com flight API failed, using mock data: {}", e.getMessage());
            List<Flight> filtered = getMockFlights().stream()
                    .filter(f -> f.getOrigin().equalsIgnoreCase(origin)
                            && f.getDestination().equalsIgnoreCase(destination))
                    .collect(Collectors.toList());
            return filtered.isEmpty() ? getMockFlights() : filtered;
        }
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
    private List<Flight> fetchFromBooking(String origin, String destination) {
        String fromCode = origin.toUpperCase() + ".CITY";
        String toCode   = destination.toUpperCase() + ".CITY";
        String date     = LocalDate.now().plusDays(7).toString();

        ResponseEntity<Map> response = restTemplate.exchange(
                SEARCH_URL, HttpMethod.GET,
                new HttpEntity<>(buildHeaders()), Map.class,
                fromCode, toCode, date);

        Map<String, Object> body = response.getBody();
        if (body == null) return Collections.emptyList();

        Map<String, Object> data = (Map<String, Object>) body.get("data");
        if (data == null) return Collections.emptyList();

        List<Map<String, Object>> flights =
                (List<Map<String, Object>>) data.get("flights");
        if (flights == null) return Collections.emptyList();

        List<Flight> result = new ArrayList<>();
        for (int i = 0; i < flights.size(); i++) {
            Flight f = mapFlight(flights.get(i), origin, destination, i + 1);
            if (f != null) result.add(f);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Flight mapFlight(Map<String, Object> raw, String origin,
                             String destination, int index) {
        try {
            String flightId    = "BK-FL-" + index;
            String airline     = "Unknown";
            String depTime     = "";
            String arrTime     = "";
            String flightNum   = "";
            double price       = 0.0;
            int    seats       = 9;

            List<Map<String, Object>> segments =
                    (List<Map<String, Object>>) raw.get("segments");
            if (segments != null && !segments.isEmpty()) {
                List<Map<String, Object>> legs =
                        (List<Map<String, Object>>) segments.get(0).get("legs");
                if (legs != null && !legs.isEmpty()) {
                    Map<String, Object> leg = legs.get(0);
                    depTime = getString(leg, "departureTime");
                    arrTime = getString(leg, "arrivalTime");

                    List<Map<String, Object>> carriers =
                            (List<Map<String, Object>>) leg.get("carriersData");
                    if (carriers != null && !carriers.isEmpty()) {
                        airline = getString(carriers.get(0), "name");
                        String code = getString(carriers.get(0), "code");
                        Map<String, Object> info =
                                (Map<String, Object>) leg.get("flightInfo");
                        String num = info != null
                                ? String.valueOf(info.getOrDefault("flightNumber", ""))
                                : "";
                        flightNum = code + num;
                    }
                }
            }

            Map<String, Object> priceBreakdown =
                    (Map<String, Object>) raw.get("priceBreakdown");
            if (priceBreakdown != null) {
                Map<String, Object> total =
                        (Map<String, Object>) priceBreakdown.get("total");
                if (total != null) {
                    long units = total.get("units") != null
                            ? ((Number) total.get("units")).longValue() : 0L;
                    long nanos = total.get("nanos") != null
                            ? ((Number) total.get("nanos")).longValue() : 0L;
                    price = units + nanos / 1_000_000_000.0;
                }
            }

            return new Flight(flightId, airline,
                    origin.toUpperCase(), destination.toUpperCase(),
                    depTime, arrTime, flightNum, price, seats);
        } catch (Exception e) {
            log.warn("Could not map flight entry #{}: {}", index, e.getMessage());
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private List<Flight> getMockFlights() {
        return Arrays.asList(
                new Flight("FL001", "Delta Airlines",    "NYC", "LAX",
                        "2024-06-01T08:00:00", "2024-06-01T11:30:00", "DL101", 299.99, 45),
                new Flight("FL002", "United Airlines",   "NYC", "LAX",
                        "2024-06-01T14:00:00", "2024-06-01T17:30:00", "UA205", 349.99, 30),
                new Flight("FL003", "American Airlines", "LAX", "NYC",
                        "2024-06-02T09:00:00", "2024-06-02T17:00:00", "AA312", 279.99, 60),
                new Flight("FL004", "Southwest Airlines","SFO", "ORD",
                        "2024-06-01T10:00:00", "2024-06-01T16:00:00", "SW415", 199.99, 80),
                new Flight("FL005", "JetBlue Airways",   "BOS", "MIA",
                        "2024-06-01T07:00:00", "2024-06-01T10:30:00", "JB520", 249.99, 55),
                new Flight("FL006", "Delta Airlines",    "MIA", "BOS",
                        "2024-06-02T13:00:00", "2024-06-02T16:30:00", "DL630", 269.99, 40),
                new Flight("FL007", "United Airlines",   "ORD", "SFO",
                        "2024-06-01T18:00:00", "2024-06-01T20:30:00", "UA740", 319.99, 25),
                new Flight("FL008", "American Airlines", "NYC", "CHI",
                        "2024-06-03T11:00:00", "2024-06-03T13:00:00", "AA850", 159.99, 70)
        );
    }
}
