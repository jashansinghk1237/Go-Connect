package com.goconnect.cabservice.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <-- YEH LINE ADD KARNI HAI

// Yeh annotation Object Mapper ko batata hai ki unknown fields ko ignore kar de
@JsonIgnoreProperties(ignoreUnknown = true) 
public record JourneyPlan(
    String startLocation,
    String finalDestination,
    float totalCost,
    List<JourneyLeg> legs
) {}








