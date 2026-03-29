package com.travel.carrental.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Car {

    private final String id;
    private final String make;
    private final String model;
    private final String type;
    private final String location;
    private final double pricePerDay;
    private final boolean available;
    private final int seats;

    @JsonCreator
    public Car(
            @JsonProperty("id") String id,
            @JsonProperty("make") String make,
            @JsonProperty("model") String model,
            @JsonProperty("type") String type,
            @JsonProperty("location") String location,
            @JsonProperty("pricePerDay") double pricePerDay,
            @JsonProperty("available") boolean available,
            @JsonProperty("seats") int seats) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.type = type;
        this.location = location;
        this.pricePerDay = pricePerDay;
        this.available = available;
        this.seats = seats;
    }

    public String getId() { return id; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable() { return available; }
    public int getSeats() { return seats; }

    @Override
    public String toString() {
        return "Car{id='" + id + "', make='" + make + "', model='" + model +
               "', type='" + type + "', location='" + location +
               "', price=" + pricePerDay + "}";
    }
}
