package com.goconnect.cabservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        File file = new File(bookingsFilePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(file, new TypeReference<List<FinalBooking>>() {});
    }
    
    public void saveBooking(FinalBooking booking) throws IOException {
        List<FinalBooking> bookings = getAllBookings();
        bookings.add(booking);
        
        objectMapper.writeValue(new File(bookingsFilePath), bookings);
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
