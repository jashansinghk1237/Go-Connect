package com.goconnect.cabservice;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                    b.journeyPlan().startLocation(), // Origin
                    b.journeyPlan().finalDestination(), // Destination
                    "12:30", // ETA
                    List.of(
                        new TrackingStop(b.journeyPlan().startLocation(), "11:00", "11:00", "Departed"),
                        new TrackingStop(b.journeyPlan().finalDestination(), "12:30", "12:30", "Upcoming")
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

