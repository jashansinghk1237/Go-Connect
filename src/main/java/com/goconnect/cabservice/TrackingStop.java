package com.goconnect.cabservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackingStop(
    String location,
    String scheduledTime,
    String estimatedTime,
    String status
) {}

