package com.goconnect.cabservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainData(
    @JsonProperty("train_name")
    String trainName,

    @JsonProperty("current_station_name")
    String currentStationName,

    @JsonProperty("current_status")
    String currentStatus,

    @JsonProperty("from_station_name")
    String fromStationName,

    @JsonProperty("to_station_name")
    String toStationName,

    @JsonProperty("eta")
    String estimatedArrivalTime,

    @JsonProperty("schedule")
    List<TrainStation> schedule
) {}
