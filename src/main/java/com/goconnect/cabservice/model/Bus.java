package com.goconnect.cabservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Bus(
    String id,
    String operatorName,
    String busType,
    String licensePlate,
    int totalSeats,
    int seatsAvailable,
    String fromCity,
    String toCity,
    String departureTime,
    String arrivalTime,
    float fare
) {}