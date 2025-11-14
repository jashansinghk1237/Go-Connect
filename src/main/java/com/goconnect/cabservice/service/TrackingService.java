package com.goconnect.cabservice.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goconnect.cabservice.model.FinalBooking;
import com.goconnect.cabservice.model.TrackingInfo;
import com.goconnect.cabservice.model.TrackingStop;

@Service
public class TrackingService {

    private final ObjectMapper mapper = new ObjectMapper();
    
    @Autowired
    private BookingService bookingService;

    public TrackingInfo getTrackingInfo(String trackingId) {
        if (trackingId == null || trackingId.length() < 2) {
            return null; // Handle invalid or empty IDs
        }
        
        // Check if it's a booking ID (starts with "GC-")
        if (trackingId.startsWith("GC-")) {
            return getTrackingInfoByBookingId(trackingId);
        }
        
        // Use the first two characters as the prefix (e.g., "C-", "B-")
        String prefix = trackingId.substring(0, 2).toUpperCase();

        // This switch statement ensures the correct file is always searched
        return switch (prefix) {
            case "C-" -> findById("classpath:tracking-cabs.json", trackingId);
            case "B-" -> findById("classpath:tracking-buses.json", trackingId);
            case "T-" -> findById("classpath:tracking-trains.json", trackingId);
            case "F-" -> findById("classpath:tracking-flights.json", trackingId);
            default -> null; // Return null if the prefix is not recognized
        };
    }
    
    private TrackingInfo getTrackingInfoByBookingId(String bookingId) {
        try {
            List<FinalBooking> bookings = bookingService.getAllBookings();
            Optional<FinalBooking> booking = bookings.stream()
                .filter(b -> b.bookingId().equals(bookingId))
                .findFirst();
            
            if (booking.isPresent()) {
                FinalBooking b = booking.get();
                // For now, return a mock tracking info based on the booking
                // In a real system, this would query the actual vehicle tracking system
                return new TrackingInfo(
                    "C-101", // Mock vehicle ID
                    "Cab", // Type
                    "Live Tracking", // Status
                    "Driver is on the way to pickup location", // Current info
                    
                    // --- YEH HAI FIX #1 ---
                    b.plan().startLocation(), // Origin
                    // --- YEH HAI FIX #2 ---
                    b.plan().finalDestination(), // Destination
                    
                    "12:30", // ETA
                    List.of(
                        // --- YEH HAI FIX #3 ---
                        new TrackingStop(b.plan().startLocation(), "11:00", "11:00", "Departed"),
                        // --- YEH HAI FIX #4 ---
                        new TrackingStop(b.plan().finalDestination(), "12:30", "12:30", "Upcoming")
                    ),
                    "Driver Name", // Driver name
                    "HR01-AB-1234", // License plate
                    "9876543210" // Driver phone
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TrackingInfo findById(String filePath, String idToFind) {
        try {
            File dataFile = ResourceUtils.getFile(filePath);
            
            // Read the file as a list of generic Maps
            List<Map<String, Object>> items = mapper.readValue(dataFile, new TypeReference<>() {});

            // Find the specific item in the list that matches the given ID
            Optional<Map<String, Object>> foundItem = items.stream()
                .filter(item -> idToFind.equalsIgnoreCase((String) item.get("id")))
                .findFirst();

            // If the item is found, convert it into our structured TrackingInfo record
            if (foundItem.isPresent()) {
                Map<String, Object> data = foundItem.get();
                // We need to manually set the "type" based on the prefix since it's not in the JSON
                data.put("type", getTypeFromPrefix(idToFind.substring(0, 2)));
                return mapper.convertValue(data, TrackingInfo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if not found or if an error occurs
    }
    
    private String getTypeFromPrefix(String prefix) {
        return switch (prefix.toUpperCase()) {
            case "C-" -> "Cab";
            case "B-" -> "Bus";
            case "T-" -> "Train";
            case "F-" -> "Flight";
            default -> "Unknown";
        };
    }
}












































































// package com.goconnect.cabservice.service;

// import java.io.IOException;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.goconnect.cabservice.model.Bus;
// import com.goconnect.cabservice.model.Cab;
// import com.goconnect.cabservice.model.FinalBooking;
// import com.goconnect.cabservice.model.Flight;
// import com.goconnect.cabservice.model.JourneyLeg;
// import com.goconnect.cabservice.model.TrackingInfo;
// import com.goconnect.cabservice.model.TrackingStop;
// import com.goconnect.cabservice.model.Train;

// @Service
// public class TrackingService {

//     @Autowired
//     private DataService dataService; // Naya DataService istemaal karein

//     @Autowired
//     private BookingService bookingService; // Booking ID se search karne ke liye

//     public TrackingInfo getTrackingInfo(String trackingId) {
//         if (trackingId == null || trackingId.isBlank()) {
//             return null;
//         }

//         // 1. Check if it's a Booking ID (e.g., "GC-123456")
//         if (trackingId.startsWith("GC-")) {
//             try {
//                 Optional<FinalBooking> bookingOpt = bookingService.getAllBookings().stream()
//                         .filter(b -> b.bookingId().equals(trackingId))
//                         .findFirst();
//                 if (bookingOpt.isPresent()) {
//                     // Booking mil gayi, ab uss booking ke pehle leg ka vehicle track karo
//                     JourneyLeg firstLeg = bookingOpt.get().plan().legs().get(0);
//                     return getTrackingInfo(firstLeg.vehicleId()); // Recursively call with Vehicle ID
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//                 return null;
//             }
//         }

//         // 2. Check if it's a Vehicle ID (e.g., "C-101", "T-12926")
//         String prefix = trackingId.substring(0, 2).toUpperCase();
        
//         switch (prefix) {
//             case "C-": // --- CAB TRACKING ---
//                 Optional<Cab> cabOpt = dataService.getCabById(Integer.parseInt(trackingId.substring(2)));
//                 if (cabOpt.isPresent()) {
//                     Cab cab = cabOpt.get();
//                     List<TrackingStop> stops = List.of(
//                         new TrackingStop(cab.currentLocation(), "N/A", "N/A", "Departed"),
//                         new TrackingStop("Destination (approx)", "N/A", "N/A", "Upcoming")
//                     );
//                     return new TrackingInfo(cab.id() + "", "Cab", "On Time", "Driver is en route", 
//                                             cab.currentLocation(), "Destination", "15 mins", stops, 
//                                             cab.driverName(), cab.licensePlate(), cab.driverPhoneNumber());
//                 }
//                 break;

//             case "B-": // --- BUS TRACKING ---
//                 Optional<Bus> busOpt = dataService.getBusById(trackingId);
//                 if (busOpt.isPresent()) {
//                     Bus bus = busOpt.get();
//                     List<TrackingStop> stops = List.of(
//                         new TrackingStop(bus.fromCity(), bus.departureTime(), bus.departureTime(), "Departed"),
//                         new TrackingStop(bus.toCity(), bus.arrivalTime(), bus.arrivalTime(), "Upcoming")
//                     );
//                     return new TrackingInfo(bus.id(), "Bus", "On Time", "Departed from " + bus.fromCity(),
//                                             bus.fromCity(), bus.toCity(), bus.arrivalTime(), stops, 
//                                             bus.operatorName(), bus.licensePlate(), "N/A");
//                 }
//                 break;

//             case "T-": // --- TRAIN TRACKING ---
//                 Optional<Train> trainOpt = dataService.getTrainById(trackingId);
//                 if (trainOpt.isPresent()) {
//                     Train train = trainOpt.get();
//                     List<TrackingStop> stops = List.of(
//                         new TrackingStop(train.fromStation(), train.departureTime(), train.departureTime(), "Departed"),
//                         new TrackingStop(train.toStation(), train.arrivalTime(), train.arrivalTime(), "Upcoming")
//                     );
//                     return new TrackingInfo(train.id(), "Train", "On Time", "Departed from " + train.fromStation(),
//                                             train.fromStation(), train.toStation(), train.arrivalTime(), stops, 
//                                             null, train.trainName() + " (" + train.trainNumber() + ")", "139");
//                 }
//                 break;

//             case "F-": // --- FLIGHT TRACKING ---
//                 Optional<Flight> flightOpt = dataService.getFlightById(trackingId);
//                 if (flightOpt.isPresent()) {
//                     Flight flight = flightOpt.get();
//                     List<TrackingStop> stops = List.of(
//                         new TrackingStop(flight.fromAirport(), flight.departureTime(), flight.departureTime(), "Departed"),
//                         new TrackingStop(flight.toAirport(), flight.arrivalTime(), flight.arrivalTime(), "Upcoming")
//                     );
//                     return new TrackingInfo(flight.id(), "Flight", "On Time", "Departed from " + flight.fromAirport(),
//                                             flight.fromAirport(), flight.toAirport(), flight.arrivalTime(), stops, 
//                                             flight.airline(), flight.flightNumber(), "N/A");
//                 }
//                 break;
//         }
        
//         return null; // ID match nahi hui
//     }
// }