package com.goconnect.cabservice.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// This tells the system to ignore any extra fields in the JSON, preventing errors
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackingInfo(
    String id,
    String type,
    String status,
    String currentInfo,
    String origin,
    String destination,
    String eta,
    List<TrackingStop> stops, // Correct name (matches JSON)
    String driverName,
    String licensePlate,
    String driverPhoneNumber // Correct name (matches JSON)
) {}