package com.goconnect.cabservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainStation(
    @JsonProperty("station_name")
    String stationName,

    @JsonProperty("scheduled_arrival")
    String scheduledArrival,

    @JsonProperty("actual_arrival")
    String actualArrival,
    
    @JsonProperty("status")
    String status,

    @JsonProperty("delay_in_minutes")
    int delayInMinutes
) {}
