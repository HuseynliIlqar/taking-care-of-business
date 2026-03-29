package com.travel.flight.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Flight {

    private final String id;
    private final String airline;
    private final String origin;
    private final String destination;
    private final String departureTime;
    private final String arrivalTime;
    private final String flightNumber;
    private final double price;
    private final int availableSeats;

    @JsonCreator
    public Flight(
            @JsonProperty("id") String id,
            @JsonProperty("airline") String airline,
            @JsonProperty("origin") String origin,
            @JsonProperty("destination") String destination,
            @JsonProperty("departureTime") String departureTime,
            @JsonProperty("arrivalTime") String arrivalTime,
            @JsonProperty("flightNumber") String flightNumber,
            @JsonProperty("price") double price,
            @JsonProperty("availableSeats") int availableSeats) {
        this.id = id;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.flightNumber = flightNumber;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    public String getId() { return id; }
    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getFlightNumber() { return flightNumber; }
    public double getPrice() { return price; }
    public int getAvailableSeats() { return availableSeats; }

    @Override
    public String toString() {
        return "Flight{id='" + id + "', airline='" + airline + "', " +
               origin + " -> " + destination + ", price=" + price + "}";
    }
}
