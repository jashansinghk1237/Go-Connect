package com.goconnect.cabservice.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinalBooking(
    String bookingId,
    JourneyPlan plan, // <-- YEH HAI FIX (Pehle 'journeyPlan' tha)
    List<Passenger> passengers
) {}

