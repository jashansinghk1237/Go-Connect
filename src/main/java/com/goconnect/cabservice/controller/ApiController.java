package com.goconnect.cabservice.controller;
// package com.goconnect.controller;

// import com.goconnect.model.*;
// import com.goconnect.repository.BookingRepository;
// import com.goconnect.repository.UserRepository;
// import com.goconnect.service.JourneyService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.time.LocalDateTime; // Import LocalDateTime
// import java.util.List;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api")
// public class ApiController {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private BookingRepository bookingRepository;

//     @Autowired
//     private JourneyService journeyService;

//     // --- User Endpoints ---

//     @GetMapping("/users/{userId}")
//     public ResponseEntity<User> getUserById(@PathVariable String userId) {
//         return userRepository.findById(userId)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     // This is the new endpoint for login
//     @GetMapping("/users/by-email")
//     public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
//         return userRepository.findByEmail(email)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping("/users")
//     public ResponseEntity<User> createUser(@RequestBody User newUser) {
//         if (newUser.getEmail() == null || userRepository.findByEmail(newUser.getEmail()).isPresent()) {
//             return ResponseEntity.status(HttpStatus.CONFLICT).build();
//         }
//         User savedUser = userRepository.save(newUser);
//         return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
//     }
    
//     // --- Journey & Booking Endpoints ---

//     @PostMapping("/plan")
//     public ResponseEntity<JourneyPlan> getJourneyPlan(@RequestBody JourneyRequest request) {
//         if (request.getStart() == null || request.getEnd() == null) {
//             return ResponseEntity.badRequest().build();
//         }
//         JourneyPlan plan = journeyService.createPlan(request.getStart(), request.getEnd());
//         if (plan == null || plan.getLegs() == null || plan.getLegs().isEmpty()) {
//             return ResponseEntity.badRequest().build();
//         }
//         return ResponseEntity.ok(plan);
//     }

//     // --- THIS IS THE CORRECTED METHOD ---
//     @PostMapping("/bookings")
//     public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
//         try {
//             // Set server-side data to ensure integrity
//             booking.setBookingTime(LocalDateTime.now());
            
//             // Get total fare from the plan and set it
//             if (booking.getPlan() != null && booking.getPlan().getTotalFare() != null) {
//                 booking.setTotalFare(booking.getPlan().getTotalFare());
//             }

//             Booking savedBooking = bookingRepository.save(booking);
//             return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
        
//         } catch (Exception e) {
//             // If anything goes wrong (e.g., plan is null), return an error
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
// }

