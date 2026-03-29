package com.travel.hotel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hotel {

    private final String id;
    private final String name;
    private final String city;
    private final String address;
    private final double rating;
    private final double pricePerNight;
    private final int availableRooms;
    private final String amenities;

    @JsonCreator
    public Hotel(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("city") String city,
            @JsonProperty("address") String address,
            @JsonProperty("rating") double rating,
            @JsonProperty("pricePerNight") double pricePerNight,
            @JsonProperty("availableRooms") int availableRooms,
            @JsonProperty("amenities") String amenities) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.rating = rating;
        this.pricePerNight = pricePerNight;
        this.availableRooms = availableRooms;
        this.amenities = amenities;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public double getPricePerNight() { return pricePerNight; }
    public int getAvailableRooms() { return availableRooms; }
    public String getAmenities() { return amenities; }

    @Override
    public String toString() {
        return "Hotel{id='" + id + "', name='" + name + "', city='" + city +
               "', rating=" + rating + ", price=" + pricePerNight + "}";
    }
}
