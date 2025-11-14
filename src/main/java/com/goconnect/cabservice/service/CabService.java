package com.goconnect.cabservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goconnect.cabservice.model.Cab;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service; // <-- THIS IMPORT FIXES THE ERROR
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // This line will now be recognized correctly
public class CabService {

    private List<Cab> allCabs = Collections.emptyList();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = TypeReference.class.getResourceAsStream("/cabs.json");
            allCabs = mapper.readValue(inputStream, new TypeReference<List<Cab>>() {});
            System.out.println("Successfully loaded " + allCabs.size() + " cabs from JSON.");
        } catch (Exception e) {
            System.err.println("Error loading cab data from JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Cab> findAvailableCabs(String location) {
        return allCabs.stream()
                .filter(Cab::isAvailable)
                .collect(Collectors.toList());
    }

    public Optional<Cab> findCabById(int id) {
        return allCabs.stream()
                .filter(cab -> cab.id() == id)
                .findFirst();
    }
}
