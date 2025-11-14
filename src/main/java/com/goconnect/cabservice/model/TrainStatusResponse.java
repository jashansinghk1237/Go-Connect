package com.goconnect.cabservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// This annotation is crucial. It tells our app to ignore any extra JSON fields 
// from the API that we don't need, preventing errors.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainStatusResponse(
    @JsonProperty("data") // Maps the "data" field in the JSON to this record component
    TrainData data
) {}
