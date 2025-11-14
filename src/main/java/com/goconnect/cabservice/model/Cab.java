package com.goconnect.cabservice.model;

public record Cab(
    int id,
    String driverName,
    String cabType,
    String licensePlate,
    double pricePerKm,
    boolean isAvailable,
    int totalSeats,
    int seatsAvailable,
    String currentLocation,
    String destination,
    String driverPhoneNumber // <-- ADD THIS NEW FIELD
) {}
