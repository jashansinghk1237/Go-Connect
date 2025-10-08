// package com.goconnect.cabservice;

// import java.util.List;

// public record JourneyPlan(
//     String passengerName,
//     String startLocation,
//     String finalDestination,
//     float totalCost,
//     List<JourneyLeg> legs
// ) {}










package com.goconnect.cabservice;

import java.util.List;

public record JourneyPlan(
    String passengerName,
    String passengerPhone, // <-- This is the field that was missing
    String startLocation,
    String finalDestination,
    float totalCost,
    List<JourneyLeg> legs
) {}

