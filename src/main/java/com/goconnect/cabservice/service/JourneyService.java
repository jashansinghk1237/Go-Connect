package com.goconnect.cabservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.goconnect.cabservice.model.JourneyLeg;
import com.goconnect.cabservice.model.JourneyPlan;

@Service
public class JourneyService {

    private double getDistance(String fromState, String toState) {
        if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Maharashtra")) return 1400;
        if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Punjab")) return 100;
        return 500;
    }
    
    // This method signature now perfectly matches the call from the controller
    public JourneyPlan planJourney(String passengerName, String passengerPhone,
                                    String startStreet, String startCity, String startState, String startCountry,
                                    String destStreet, String destCity, String destState, String destCountry) {
        
        List<JourneyLeg> legs = new ArrayList<>();
        float totalCost = 0;
        String defaultDeparture = "10:00 AM";

        if (!startCountry.equalsIgnoreCase(destCountry)) {
            legs.add(new JourneyLeg(1, "Cab", "C-101", startStreet + ", " + startCity, "Airport (DEL)", "Local Cab", 800f, defaultDeparture));
            legs.add(new JourneyLeg(2, "Flight", "F-AI101", "Delhi (DEL)", destCountry + " Airport", "Intl Flight", 80000f, "05:00 PM"));
            legs.add(new JourneyLeg(3, "Cab", "C-202", destCountry + " Airport", destStreet + ", " + destCity, "Local Cab", 1500f, "09:00 AM"));
            totalCost = 82300f;
        } 
        else if (!startState.equalsIgnoreCase(destState)) {
            double distance = getDistance(startState, destState);
            if (distance > 300) {
                legs.add(new JourneyLeg(1, "Cab", "C-102", startStreet + ", " + startCity, "Railway Station", "Local Cab", 150f, defaultDeparture));
                legs.add(new JourneyLeg(2, "Train", "T-12926", startCity, destCity, "Express Train", 2000f, "12:00 PM"));
                legs.add(new JourneyLeg(3, "Cab", "C-203", "Railway Station", destStreet + ", " + destCity, "Local Cab", 250f, "08:00 PM"));
                totalCost = 2400f;
            } else {
                legs.add(new JourneyLeg(1, "Cab", "C-103", startStreet + ", " + startCity, "Bus Stand", "Local Cab", 120f, defaultDeparture));
                legs.add(new JourneyLeg(2, "Bus", "B-101", startCity, destCity, "Volvo Bus", 700f, "11:30 AM"));
                legs.add(new JourneyLeg(3, "Cab", "C-204", "Bus Stand", destStreet + ", " + destCity, "Local Cab", 180f, "03:00 PM"));
                totalCost = 1000f;
            }
        } 
        else if (!startCity.equalsIgnoreCase(destCity)) {
            legs.add(new JourneyLeg(1, "Cab", "C-104", startStreet + ", " + startCity, "Bus Stand", "Local Cab", 100f, defaultDeparture));
            legs.add(new JourneyLeg(2, "Bus", "B-202", startCity, destCity, "Express Bus", 300f, "11:00 AM"));
            legs.add(new JourneyLeg(3, "Cab", "C-205", "Bus Stand", destStreet + ", " + destCity, "Local Cab", 150f, "01:00 PM"));
            totalCost = 550f;
        } 
        else {
            legs.add(new JourneyLeg(1, "Cab", "C-105", startStreet + ", " + startCity, destStreet + ", " + destCity, "City Cab", 400f, defaultDeparture));
            totalCost = 400f;
        }

        String startLocation = startCity + ", " + startCountry;
        String finalDestination = destCity + ", " + destCountry;
        
        // --- YEH HAI FIX ---
        // Ab hum constructor ko 4 arguments bhej rahe hain, jo JourneyPlan expect kar raha hai
        return new JourneyPlan(startLocation, finalDestination, totalCost, legs);
    }
}
















































































