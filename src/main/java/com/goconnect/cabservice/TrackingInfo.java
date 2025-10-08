package com.goconnect.cabservice;

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
    List<TrackingStop> schedule,
    String driverName,
    String licensePlate,
    String driverPhone
) {}

