package com.goconnect.cabservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goconnect.cabservice.model.Bus;
import com.goconnect.cabservice.model.Cab;
import com.goconnect.cabservice.model.Flight;
import com.goconnect.cabservice.model.Train;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Cab> cabs = new ArrayList<>();
    private List<Bus> buses = new ArrayList<>();
    private List<Train> trains = new ArrayList<>();
    private List<Flight> flights = new ArrayList<>();

    // Jab server start hoga, yeh function automatically JSON files ko load kar lega
    @PostConstruct
    public void loadData() {
        try {
            cabs = loadJson("data/cabs.json", new TypeReference<List<Cab>>() {});
            buses = loadJson("data/buses.json", new TypeReference<List<Bus>>() {});
            trains = loadJson("data/trains.json", new TypeReference<List<Train>>() {});
            flights = loadJson("data/flights.json", new TypeReference<List<Flight>>() {});
            System.out.println("--- All data loaded successfully ---");
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> List<T> loadJson(String path, TypeReference<List<T>> type) throws Exception {
        InputStream inputStream = new ClassPathResource(path).getInputStream();
        return objectMapper.readValue(inputStream, type);
    }

    // --- Data ko search karne ke liye functions ---

    public Optional<Cab> findAvailableCab(String city, int seats) {
        return cabs.stream()
                .filter(cab -> cab.isAvailable() && cab.seatsAvailable() >= seats && cab.currentLocation().equalsIgnoreCase(city))
                .findFirst();
    }
    
    public Optional<Cab> getCabById(int id) {
         return cabs.stream().filter(cab -> cab.id() == id).findFirst();
    }

    public Optional<Bus> findBus(String from, String to) {
        return buses.stream()
                .filter(bus -> bus.fromCity().equalsIgnoreCase(from) && bus.toCity().equalsIgnoreCase(to))
                .findFirst();
    }
    
    public Optional<Bus> getBusById(String id) {
         return buses.stream().filter(bus -> bus.id().equals(id)).findFirst();
    }

    public Optional<Train> findTrain(String from, String to) {
        return trains.stream()
                .filter(train -> train.fromStation().equalsIgnoreCase(from) && train.toStation().equalsIgnoreCase(to))
                .findFirst();
    }

    public Optional<Train> getTrainById(String id) {
         return trains.stream().filter(train -> train.id().equals(id)).findFirst();
    }

    public Optional<Flight> findFlight(String fromCountry, String toCountry) {
        return flights.stream()
                .filter(flight -> flight.fromAirport().equalsIgnoreCase(fromCountry) && flight.toAirport().equalsIgnoreCase(toCountry))
                .findFirst();
    }
    
    public Optional<Flight> getFlightById(String id) {
         return flights.stream().filter(flight -> flight.id().equals(id)).findFirst();
    }
    
    // Yahaan seats update karne ke function bhi add honge
    // (Abhi ke liye simple rakhte hain)
}