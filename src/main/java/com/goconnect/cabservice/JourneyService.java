package com.goconnect.cabservice;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
        
        return new JourneyPlan(passengerName, passengerPhone, startLocation, finalDestination, totalCost, legs);
    }
}































// package com.goconnect.cabservice;

// import org.springframework.stereotype.Service;
// import java.util.ArrayList;
// import java.util.List;

// @Service
// public class JourneyService {

//     private double getDistance(String fromState, String toState) {
//         if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Maharashtra")) {
//             return 1400;
//         }
//         if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Punjab")) {
//             return 100;
//         }
//         return 500;
//     }
    
//     public JourneyPlan planJourney(String passengerName, String passengerPhone,
//                                    String startStreet, String startCity, String startState, String startCountry,
//                                    String destStreet, String destCity, String destState, String destCountry) {
        
//         List<JourneyLeg> legs = new ArrayList<>();
//         float totalCost = 0;
//         String defaultDeparture = "10:00 AM"; // Default time for the plan

//         // Rule 1: Different Country -> Use Flight
//         if (!startCountry.equalsIgnoreCase(destCountry)) {
//             legs.add(new JourneyLeg(1, "Cab", "C-101", startStreet + ", " + startCity, "Nearest International Airport (DEL)", "Local Cab to Airport", 800f, defaultDeparture));
//             legs.add(new JourneyLeg(2, "Flight", "F-AI101", "Delhi (DEL), " + startCountry, destCountry + " Airport", "International Flight", 80000f, "05:00 PM"));
//             legs.add(new JourneyLeg(3, "Cab", "C-202", destCountry + " Airport", destStreet + ", " + destCity, "Local Cab from Airport", 1500f, "09:00 AM (Local)"));
//             totalCost = 82300f;
//         } 
//         // Rule 2: Different State -> Use Train or Bus
//         else if (!startState.equalsIgnoreCase(destState)) {
//             double distance = getDistance(startState, destState);
//             if (distance > 300) { // Use Train
//                 legs.add(new JourneyLeg(1, "Cab", "C-102", startStreet + ", " + startCity, startCity + " Railway Station", "Local Cab to Station", 150f, defaultDeparture));
//                 legs.add(new JourneyLeg(2, "Train", "T-12926", startCity + " Railway Station", destCity + " Railway Station", "Inter-State Express Train", 2000f, "12:00 PM"));
//                 legs.add(new JourneyLeg(3, "Cab", "C-203", destCity + " Railway Station", destStreet + ", " + destCity, "Local Cab from Station", 250f, "08:00 PM"));
//                 totalCost = 2400f;
//             } else { // Use Bus
//                 legs.add(new JourneyLeg(1, "Cab", "C-103", startStreet + ", " + startCity, startCity + " Bus Stand", "Local Cab to Bus Stand", 120f, defaultDeparture));
//                 legs.add(new JourneyLeg(2, "Bus", "B-101", startCity + " Bus Stand", destCity + " Bus Stand", "Inter-State Volvo Bus", 700f, "11:30 AM"));
//                 legs.add(new JourneyLeg(3, "Cab", "C-204", destCity + " Bus Stand", destStreet + ", " + destCity, "Local Cab from Bus Stand", 180f, "03:00 PM"));
//                 totalCost = 1000f;
//             }
//         } 
//         // Rule 3: Different City -> Use Bus
//         else if (!startCity.equalsIgnoreCase(destCity)) {
//             legs.add(new JourneyLeg(1, "Cab", "C-104", startStreet + ", " + startCity, startCity + " Bus Stand", "Local Cab to Bus Stand", 100f, defaultDeparture));
//             legs.add(new JourneyLeg(2, "Bus", "B-202", startCity + " Bus Stand", destCity + " Bus Stand", "Inter-City Express Bus", 300f, "11:00 AM"));
//             legs.add(new JourneyLeg(3, "Cab", "C-205", destCity + " Bus Stand", destStreet + ", " + destCity, "Local Cab from Bus Stand", 150f, "01:00 PM"));
//             totalCost = 550f;
//         } 
//         // Rule 4: Same City -> Use Cab
//         else {
//             legs.add(new JourneyLeg(1, "Cab", "C-105", startStreet + ", " + startCity, destStreet + ", " + destCity, "Local City Cab", 400f, defaultDeparture));
//             totalCost = 400f;
//         }

//         String startLocation = startCity + ", " + startCountry;
//         String finalDestination = destCity + ", " + destCountry;
        
//         return new JourneyPlan(passengerName, passengerPhone, startLocation, finalDestination, totalCost, legs);
//     }
// }




























// package com.goconnect.cabservice;

// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.stereotype.Service;

// @Service
// public class JourneyService {

//     // Simple mock for the > 300km state-to-state rule
//     private double getDistance(String fromState, String toState) {
//         if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Maharashtra")) {
//             return 1400; // e.g., Ambala to Mumbai
//         }
//         if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Punjab")) {
//             return 100; // e.g., Ambala to Ludhiana
//         }
//         return 500; // Default
//     }
    
//     // The new, rule-based "Brain"
//     public JourneyPlan planJourney(String passengerName, String startStreet, String startCity, String startState, String startCountry,
//                                    String destStreet, String destCity, String destState, String destCountry) {
        
//         List<JourneyLeg> legs = new ArrayList<>();
//         float totalCost = 0;

//         // Rule 1: Different Country -> Use Flight
//         if (!startCountry.equalsIgnoreCase(destCountry)) {
//             legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, "Nearest International Airport (DEL)", "Local Cab to Airport", 800));
//             legs.add(new JourneyLeg(2, "Flight", "Delhi (DEL), " + startCountry, destCountry + " Airport", "International Flight", 80000));
//             legs.add(new JourneyLeg(3, "Cab", destCountry + " Airport", destStreet + ", " + destCity, "Local Cab from Airport", 1500));
//             totalCost = 82300;
//         } 
//         // Rule 2: Different State -> Use Train or Bus based on distance
//         else if (!startState.equalsIgnoreCase(destState)) {
//             double distance = getDistance(startState, destState);
//             if (distance > 300) { // Use Train
//                 legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, startCity + " Railway Station", "Local Cab to Station", 150));
//                 legs.add(new JourneyLeg(2, "Train", startCity + " Railway Station", destCity + " Railway Station", "Inter-State Express Train", 2000));
//                 legs.add(new JourneyLeg(3, "Cab", destCity + " Railway Station", destStreet + ", " + destCity, "Local Cab from Station", 250));
//                 totalCost = 2400;
//             } else { // Use Bus
//                 legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, startCity + " Bus Stand", "Local Cab to Bus Stand", 120));
//                 legs.add(new JourneyLeg(2, "Bus", startCity + " Bus Stand", destCity + " Bus Stand", "Inter-State Volvo Bus", 700));
//                 legs.add(new JourneyLeg(3, "Cab", destCity + " Bus Stand", destStreet + ", " + destCity, "Local Cab from Bus Stand", 180));
//                 totalCost = 1000;
//             }
//         } 
//         // Rule 3: Different City -> Use Bus
//         else if (!startCity.equalsIgnoreCase(destCity)) {
//             legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, startCity + " Bus Stand", "Local Cab to Bus Stand", 100));
//             legs.add(new JourneyLeg(2, "Bus", startCity + " Bus Stand", destCity + " Bus Stand", "Inter-City Express Bus", 300));
//             legs.add(new JourneyLeg(3, "Cab", destCity + " Bus Stand", destStreet + ", " + destCity, "Local Cab from Bus Stand", 150));
//             totalCost = 550;
//         } 
//         // Rule 4: Same City -> Use Cab
//         else {
//             legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, destStreet + ", " + destCity, "Local City Cab", 400));
//             totalCost = 400;
//         }

//         String startLocation = startCity + ", " + startCountry;
//         String finalDestination = destCity + ", " + destCountry;
        
//         return new JourneyPlan(passengerName, startLocation, finalDestination, totalCost, legs);
//     }
// }














































// package com.goconnect.cabservice;

// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.stereotype.Service;

// @Service
// public class JourneyService {

//     // --- THIS IS OUR SIMULATED GOOGLE MAPS API ---
//     // In a real app, this would make an API call. Here, we use mock data.
//     private double getDistance(String from, String to) {
//         System.out.println("Simulating distance calculation from " + from + " to " + to);
//         if (from.contains("Ambala") && to.contains("Delhi")) return 200;
//         if (from.contains("Ambala") && to.contains("USA")) return 11000;
//         if (from.contains("Ambala") && to.contains("Chandigarh")) return 50;
//         if (from.contains("Ambala") && from.contains("local")) return 15; // A local trip
//         return 500; // Default distance
//     }
    
//     // This is the main "Brain" method
//     public JourneyPlan planJourney(String passengerName, String start, String destination) {
//         double distance = getDistance(start, destination);
//         List<JourneyLeg> legs = new ArrayList<>();
//         double totalCost = 0;

//         // Apply your rules based on distance
//         if (distance > 1000) {
//             // International/Long-Haul Flight Plan
//             legs.add(new JourneyLeg(1, "Cab", "Ambala Home", "Ambala Cantt Station", "Local Cab", 200));
//             legs.add(new JourneyLeg(2, "Train", "Ambala Cantt Station", "Delhi Airport Metro", "Shatabdi Express", 650));
//             legs.add(new JourneyLeg(3, "Flight", "Delhi (DEL)", "USA (JFK)", "Air India AI-101", 75000));
//             totalCost = 75850;
//         } else if (distance >= 200 && distance <= 1000) {
//             // Inter-city Train Plan
//             legs.add(new JourneyLeg(1, "Cab", "Ambala Home", "Ambala Cantt Station", "Local Cab", 200));
//             legs.add(new JourneyLeg(2, "Train", "Ambala Cantt Station", "Delhi Station", "Shatabdi Express", 600));
//             legs.add(new JourneyLeg(3, "Cab", "Delhi Station", "Delhi Destination", "Local Cab", 300));
//             totalCost = 1100;
//         } else if (distance >= 20 && distance < 200) {
//             // Inter-city Bus Plan
//             legs.add(new JourneyLeg(1, "Cab", "Ambala Home", "Ambala Bus Stand", "Local Auto", 100));
//             legs.add(new JourneyLeg(2, "Bus", "Ambala Bus Stand", "Destination City", "Haryana Roadways", 250));
//             totalCost = 350;
//         } else {
//             // Local Cab Plan
//             legs.add(new JourneyLeg(1, "Cab", "Ambala Start", "Ambala Destination", "Local Cab", 200));
//             totalCost = 200;
//         }

//         return new JourneyPlan(passengerName, start, destination, totalCost, legs);
//     }
// }



























































// package com.goconnect.cabservice;

// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.stereotype.Service;

// @Service
// public class JourneyService {

//     // The distance simulation is still useful for your state-to-state rule.
//     private double getDistance(String fromState, String toState) {
//         // Simple mock for the > 300km rule
//         if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Maharashtra")) {
//             return 1400; // e.g., Ambala to Mumbai
//         }
//         if (fromState.equalsIgnoreCase("Haryana") && toState.equalsIgnoreCase("Punjab")) {
//             return 100; // e.g., Ambala to Ludhiana
//         }
//         return 500; // Default
//     }
    
//     // THE NEW, RULE-BASED "BRAIN"
//     public JourneyPlan planJourney(String passengerName, String startStreet, String startCity, String startState, String startCountry,
//                                    String destStreet, String destCity, String destState, String destCountry) {
        
//         List<JourneyLeg> legs = new ArrayList<>();
//         double totalCost = 0;

//         // Rule 1: Different Country -> Use Flight
//         if (!startCountry.equalsIgnoreCase(destCountry)) {
//             legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, "Nearest International Airport (DEL)", "Local Cab to Airport", 800));
//             legs.add(new JourneyLeg(2, "Flight", "Delhi (DEL), " + startCountry, destCountry + " Airport", "International Flight", 80000));
//             legs.add(new JourneyLeg(3, "Cab", destCountry + " Airport", destStreet + ", " + destCity, "Local Cab from Airport", 1500));
//             totalCost = 82300;
//         } 
//         // Rule 2: Different State -> Use Train or Bus based on distance
//         else if (!startState.equalsIgnoreCase(destState)) {
//             double distance = getDistance(startState, destState);
//             if (distance > 300) { // Use Train
//                 legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, startCity + " Railway Station", "Local Cab to Station", 150));
//                 legs.add(new JourneyLeg(2, "Train", startCity + " Railway Station", destCity + " Railway Station", "Inter-State Express Train", 2000));
//                 legs.add(new JourneyLeg(3, "Cab", destCity + " Railway Station", destStreet + ", " + destCity, "Local Cab from Station", 250));
//                 totalCost = 2400;
//             } else { // Use Bus
//                 legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, startCity + " Bus Stand", "Local Cab to Bus Stand", 120));
//                 legs.add(new JourneyLeg(2, "Bus", startCity + " Bus Stand", destCity + " Bus Stand", "Inter-State Volvo Bus", 700));
//                 legs.add(new JourneyLeg(3, "Cab", destCity + " Bus Stand", destStreet + ", " + destCity, "Local Cab from Bus Stand", 180));
//                 totalCost = 1000;
//             }
//         } 
//         // Rule 3: Different City -> Use Bus
//         else if (!startCity.equalsIgnoreCase(destCity)) {
//             legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, startCity + " Bus Stand", "Local Cab to Bus Stand", 100));
//             legs.add(new JourneyLeg(2, "Bus", startCity + " Bus Stand", destCity + " Bus Stand", "Inter-City Express Bus", 300));
//             legs.add(new JourneyLeg(3, "Cab", destCity + " Bus Stand", destStreet + ", " + destCity, "Local Cab from Bus Stand", 150));
//             totalCost = 550;
//         } 
//         // Rule 4: Same City -> Use Cab
//         else {
//             legs.add(new JourneyLeg(1, "Cab", startStreet + ", " + startCity, destStreet + ", " + destCity, "Local City Cab", 400));
//             totalCost = 400;
//         }

//         String startLocation = startCity + ", " + startCountry;
//         String finalDestination = destCity + ", " + destCountry;
        
//         return new JourneyPlan(passengerName, startLocation, finalDestination, totalCost, legs);
//     }
// }
