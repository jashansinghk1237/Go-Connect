package com.goconnect.cabservice.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goconnect.cabservice.model.Cab;
import com.goconnect.cabservice.model.FinalBooking;
import com.goconnect.cabservice.model.JourneyLeg;
import com.goconnect.cabservice.model.JourneyPlan;
import com.goconnect.cabservice.model.Passenger;

@Service
public class BookingService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String cabsFilePath = "src/main/resources/cabs.json";
    private final String bookingsFilePath = "src/main/resources/final-bookings.json";
    
    public List<Cab> getAllCabs() throws IOException {
        File file = new File(cabsFilePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(file, new TypeReference<List<Cab>>() {});
    }
    
    public List<FinalBooking> getAllBookings() throws IOException {
        File file = ResourceUtils.getFile(bookingsFilePath);
        if (!file.exists() || file.length() == 0) {
            // If the file doesn't exist or is empty, return an empty list
            // and create an empty JSON array in the file to avoid future errors.
            Files.write(Paths.get(file.toURI()), "[]".getBytes());
            return new ArrayList<>();
        }
        return objectMapper.readValue(file, new TypeReference<List<FinalBooking>>() {});
    }
    
    public void saveBooking(FinalBooking booking) throws IOException {
        List<FinalBooking> bookings = getAllBookings();
        bookings.add(booking);
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(bookingsFilePath), bookings);
    }
    
    public void updateCabSeats(int cabId, int seatsToBook) throws IOException {
        List<Cab> cabs = getAllCabs();
        
        for (Cab cab : cabs) {
            if (cab.id() == cabId) {
                int newSeatsAvailable = cab.seatsAvailable() - seatsToBook;
                if (newSeatsAvailable < 0) {
                    throw new RuntimeException("Not enough seats available in cab " + cabId);
                }
                
                // Update the cab in the list
                Cab updatedCab = new Cab(
                    cab.id(),
                    cab.driverName(),
                    cab.cabType(),
                    cab.licensePlate(),
                    cab.pricePerKm(),
                    cab.isAvailable(),
                    cab.totalSeats(),
                    newSeatsAvailable,
                    cab.currentLocation(),
                    cab.destination(),
                    cab.driverPhoneNumber()
                );
                
                cabs.set(cabs.indexOf(cab), updatedCab);
                break;
            }
        }
        
        objectMapper.writeValue(new File(cabsFilePath), cabs);
    }
    
    public List<Cab> findAvailableCabs(int requiredSeats) throws IOException {
        List<Cab> allCabs = getAllCabs();
        List<Cab> availableCabs = new ArrayList<>();
        
        for (Cab cab : allCabs) {
            if (cab.isAvailable() && cab.seatsAvailable() >= requiredSeats) {
                availableCabs.add(cab);
            }
        }
        
        return availableCabs;
    }
    
    public Cab findBestCabForPassengers(int requiredSeats, String transportType) throws IOException {
        List<Cab> availableCabs = findAvailableCabs(requiredSeats);
        
        if (availableCabs.isEmpty()) {
            return null;
        }
        
        // Find the cab with the most available seats that can accommodate all passengers
        Optional<Cab> bestCab = availableCabs.stream()
            .filter(cab -> cab.seatsAvailable() >= requiredSeats)
            .min((c1, c2) -> Integer.compare(c1.seatsAvailable(), c2.seatsAvailable()));
        
        return bestCab.orElse(null);
    }
    
    public BookingResult bookCabsForJourney(JourneyPlan plan, List<Passenger> passengers) throws IOException {
        List<BookingResult.CabBooking> cabBookings = new ArrayList<>();
        int totalPassengers = passengers.size();
        
        // For each leg of the journey, find and book appropriate cabs
        for (JourneyLeg leg : plan.legs()) {
            if (leg.transportType().equals("Cab")) {
                Cab bestCab = findBestCabForPassengers(totalPassengers, leg.transportType());
                
                if (bestCab == null) {
                    throw new RuntimeException("No available cabs found for " + totalPassengers + " passengers");
                }
                
                // Book the cab
                updateCabSeats(bestCab.id(), totalPassengers);
                
                cabBookings.add(new BookingResult.CabBooking(
                    bestCab.id(),
                    bestCab.driverName(),
                    bestCab.licensePlate(),
                    bestCab.driverPhoneNumber(),
                    leg.from(),
                    leg.to(),
                    leg.price()
                ));
            }
        }
        
        return new BookingResult(cabBookings, true, "Booking successful");
    }
    
    public record BookingResult(
        List<CabBooking> cabBookings,
        boolean success,
        String message
    ) {
        public record CabBooking(
            int cabId,
            String driverName,
            String licensePlate,
            String driverPhone,
            String from,
            String to,
            float price
        ) {}
    }
}























































































// package com.goconnect.cabservice.service;

// import java.io.File;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.util.ResourceUtils;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.goconnect.cabservice.model.Cab;
// import com.goconnect.cabservice.model.FinalBooking;
// import com.goconnect.cabservice.model.JourneyLeg;
// import com.goconnect.cabservice.model.JourneyPlan;
// import com.goconnect.cabservice.model.Passenger;

// @Service
// public class BookingService {
    
//     @Autowired
//     private DataService dataService; // Ab hum DataService ka istemaal karenge

//     private final ObjectMapper objectMapper = new ObjectMapper();
//     private final String bookingsFilePath = "src/main/resources/final-bookings.json";
    
//     // Yeh function waise hi kaam karega, bookings ko save/load karne ke liye
//     public List<FinalBooking> getAllBookings() throws IOException {
//         File file = ResourceUtils.getFile(bookingsFilePath);
//         if (!file.exists() || file.length() == 0) {
//             Files.write(Paths.get(file.toURI()), "[]".getBytes());
//             return new ArrayList<>();
//         }
//         try {
//             return objectMapper.readValue(file, new TypeReference<List<FinalBooking>>() {});
//         } catch (IOException e) {
//             System.err.println("Error reading bookings file: " + e.getMessage());
//             Files.write(Paths.get(file.toURI()), "[]".getBytes()); // Corrupt file ko reset karo
//             return new ArrayList<>();
//         }
//     }
    
//     public void saveBooking(FinalBooking booking) throws IOException {
//         List<FinalBooking> bookings = getAllBookings();
//         bookings.add(booking);
//         objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(bookingsFilePath), bookings);
//     }
    
//     // Yeh function ab DataService se cab dhoondhega
//     public Cab findBestCabForPassengers(int requiredSeats, String fromCity) throws IOException {
//         Optional<Cab> bestCab = dataService.findAvailableCab(fromCity, requiredSeats);
//         return bestCab.orElse(null);
//     }
    
//     // Yeh function ab DataService se cab lega
//     public BookingResult bookCabsForJourney(JourneyPlan plan, List<Passenger> passengers) throws IOException {
//         List<BookingResult.CabBooking> cabBookings = new ArrayList<>();
//         int totalPassengers = passengers.size();
        
//         for (JourneyLeg leg : plan.legs()) {
//             if ("Cab".equalsIgnoreCase(leg.transportType())) {
//                 // Vehicle ID (e.g., "101") ko integer mein convert karo
//                 int cabId = Integer.parseInt(leg.vehicleId());
                
//                 Optional<Cab> cabOpt = dataService.getCabById(cabId);
                
//                 if (cabOpt.isEmpty()) {
//                     throw new RuntimeException("No available cabs found for " + totalPassengers + " passengers for leg: " + leg.details());
//                 }
                
//                 Cab bestCab = cabOpt.get();
                
//                 // TODO: Yahaan cab ki seats update karne ka logic add hoga
//                 // dataService.updateCabSeats(bestCab.id(), totalPassengers);
                
//                 cabBookings.add(new BookingResult.CabBooking(
//                     bestCab.id(),
//                     bestCab.driverName(),
//                     bestCab.licensePlate(),
//                     bestCab.driverPhoneNumber(),
//                     leg.from(),
//                     leg.to(),
//                     leg.price()
//                 ));
//             }
//         }
        
//         return new BookingResult(cabBookings, true, "Booking successful");
//     }
    
//     // --- Inner classes waise hi rahenge ---
    
//     public record BookingResult(
//         List<CabBooking> cabBookings,
//         boolean success,
//         String message
//     ) {
//         public record CabBooking(
//             int cabId,
//             String driverName,
//             String licensePlate,
//             String driverPhone,
//             String from,
//             String to,
//             float price
//         ) {}
//     }
// }