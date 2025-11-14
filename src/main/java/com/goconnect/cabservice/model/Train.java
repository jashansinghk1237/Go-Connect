package com.goconnect.cabservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Train(
    String id,
    String trainName,
    String trainNumber,
    int totalSeats,
    int seatsAvailable,
    String fromStation,
    String toStation,
    String departureTime,
    String arrivalTime,
    float fare
) {}