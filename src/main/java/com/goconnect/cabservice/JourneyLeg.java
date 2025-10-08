// package com.goconnect.cabservice;

// public record JourneyLeg(
//     int legNumber,
//     String transportType,
//     String from,
//     String to,
//     String details,
//     float price
// ) {}











package com.goconnect.cabservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// This annotation prevents errors if the JSON has extra fields we don't need.
@JsonIgnoreProperties(ignoreUnknown = true)
public record JourneyLeg(
    int legNumber,
    String transportType,
    String vehicleId,        // Crucial for looking up live data (e.g., "C-101", "T-12014")
    String from,
    String to,
    String details,
    float price,
    String estimatedDeparture // Will be populated with live data on the final ticket
) {}

