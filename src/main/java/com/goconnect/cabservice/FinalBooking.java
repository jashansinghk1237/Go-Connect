package com.goconnect.cabservice;

import java.util.List;

// Represents the entire confirmed booking with all details
public record FinalBooking(
    String bookingId,
    JourneyPlan journeyPlan,
    List<Passenger> passengers
) {}
