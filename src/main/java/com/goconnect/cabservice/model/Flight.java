package com.goconnect.cabservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Flight(
    String id,
    String airline,
    String flightNumber,
    int totalSeats,
    int seatsAvailable,
    String fromAirport,
    String toAirport,
    String departureTime,
    String arrivalTime,
    float fare
) {}