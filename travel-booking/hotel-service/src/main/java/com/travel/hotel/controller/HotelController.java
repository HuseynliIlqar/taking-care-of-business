package com.travel.hotel.controller;

import com.travel.hotel.model.Hotel;
import com.travel.hotel.service.HotelService;
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
@RequestMapping("/hotels")
@Api(tags = "Hotel API", description = "Operations for searching and retrieving hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    @ApiOperation(value = "Get all hotels", notes = "Returns a list of all available hotels")
    public ResponseEntity<List<Hotel>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/search")
    @ApiOperation(
            value = "Search hotels",
            notes  = "Search hotels by city name. "
                   + "Available cities — US (mock data): LosAngeles, NewYork, Miami, Chicago, SanFrancisco, Boston. "
                   + "European cities (live Booking.com API when key is configured): "
                   + "Prague, London, Paris, Berlin, Rome, Amsterdam.")
    public ResponseEntity<List<Hotel>> searchHotels(
            @ApiParam(value = "City name to search hotels in", required = true, example = "LosAngeles")
            @RequestParam String location) {
        List<Hotel> hotels = hotelService.searchHotels(location);
        if (hotels.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hotels);
    }
}
