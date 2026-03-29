package com.travel.flight.controller;

import com.travel.flight.model.Flight;
import com.travel.flight.service.FlightService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flights")
@Api(tags = "Flight API", description = "Operations for searching and retrieving flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    @ApiOperation(value = "Get all flights", notes = "Returns a list of all available flights")
    public ResponseEntity<List<Flight>> getAllFlights() {
        List<Flight> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/search")
    @ApiOperation(
            value = "Search flights",
            notes  = "Search flights by origin and destination airport/city code. "
                   + "Available routes: NYC→LAX, LAX→NYC, SFO→ORD, ORD→SFO, BOS→MIA, MIA→BOS, NYC→CHI")
    public ResponseEntity<List<Flight>> searchFlights(
            @ApiParam(value = "Departure city or airport code", required = true, example = "NYC")
            @RequestParam String origin,
            @ApiParam(value = "Arrival city or airport code", required = true, example = "LAX")
            @RequestParam String destination) {
        List<Flight> flights = flightService.searchFlights(origin, destination);
        if (flights.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(flights);
    }
}
