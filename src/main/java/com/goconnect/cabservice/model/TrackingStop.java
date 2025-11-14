package com.goconnect.cabservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackingStop(
    String name, // FIX 3: "location" ko "name" kar diya
    String scheduledTime,
    String actualTime, // FIX 4: "estimatedTime" ko "actualTime" kar diya
    String status
) {}