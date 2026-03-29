package com.travel.carrental.controller;

import com.travel.carrental.model.Car;
import com.travel.carrental.service.CarRentalService;
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
@RequestMapping("/cars")
@Api(tags = "Car Rental API", description = "Operations for searching and retrieving rental cars")
public class CarRentalController {

    private final CarRentalService carRentalService;

    public CarRentalController(CarRentalService carRentalService) {
        this.carRentalService = carRentalService;
    }

    @GetMapping
    @ApiOperation(value = "Get all cars", notes = "Returns a list of all available rental cars")
    public ResponseEntity<List<Car>> getAllCars() {
        List<Car> cars = carRentalService.getAllCars();
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/search")
    @ApiOperation(
            value = "Search cars",
            notes  = "Search rental cars by location and/or vehicle type. "
                   + "European cities return live Booking.com data (Prague, London, Paris, Berlin, Rome, Amsterdam). "
                   + "US cities return mock data (LosAngeles, NewYork, Miami, Chicago). "
                   + "Available types: Economy, Compact, Sedan, SUV, Luxury, Electric.")
    public ResponseEntity<List<Car>> searchCars(
            @ApiParam(value = "Pickup city", example = "Prague")
            @RequestParam(required = false) String location,
            @ApiParam(value = "Vehicle type", example = "Economy",
                      allowableValues = "Economy, Compact, Sedan, SUV, Luxury, Electric")
            @RequestParam(required = false) String type) {
        List<Car> cars = carRentalService.searchCars(location, type);
        if (cars.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cars);
    }
}
