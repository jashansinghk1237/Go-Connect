// package com.goconnect.cabservice.controller;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.goconnect.cabservice.model.FinalBooking;
// import com.goconnect.cabservice.model.JourneyPlan;
// import com.goconnect.cabservice.model.Passenger;
// import com.goconnect.cabservice.model.TrackingInfo;
// import com.goconnect.cabservice.service.BookingService;
// import com.goconnect.cabservice.service.JourneyService;
// import com.goconnect.cabservice.service.PdfService;
// import com.goconnect.cabservice.service.TrackingService;
// import com.goconnect.cabservice.service.UserService;

// @Controller
// public class JourneyController {

//     @Autowired
//     private JourneyService journeyService;
    
//     @Autowired
//     private UserService userService;
    
//     @Autowired
//     private BookingService bookingService;
    
//     @Autowired
//     private PdfService pdfService;

//     @GetMapping("/")
//     public String showPlannerPage() {
//         return "journey-planner";
//     }

//     @PostMapping("/plan-journey")
//     public String planJourney(
//             @RequestParam String passengerName,
//             @RequestParam String passengerPhone,
//             @RequestParam String startStreet, 
//             @RequestParam String startLocality,
//             @RequestParam String startCity, 
//             @RequestParam String startState, 
//             @RequestParam String startCountry,
//             @RequestParam String destStreet, 
//             @RequestParam String destLocality,
//             @RequestParam String destCity, 
//             @RequestParam String destState, 
//             @RequestParam String destCountry,
//             Model model) {
        
//         JourneyPlan plan = journeyService.planJourney(passengerName, passengerPhone, startStreet, startCity, startState, startCountry, 
//                                                        destStreet, destCity, destState, destCountry);
        
//         model.addAttribute("plan", plan);
//         try {
//             String planJson = new ObjectMapper().writeValueAsString(plan);
//             model.addAttribute("planJson", planJson);
//         } catch (Exception ignored) {}
//         return "journey-results";
//     }
    
//     @PostMapping("/proceed-to-payment")
//     public String showPaymentPage(@RequestParam float totalCost, 
//                                   @RequestParam String journeySummary, 
//                                   @AuthenticationPrincipal UserDetails userDetails, 
//                                   Model model) {
        
//         model.addAttribute("totalCost", totalCost);
//         model.addAttribute("journeySummary", journeySummary);
        
//         userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//             model.addAttribute("currentBalance", user.getGoMoneyBalance());
//         });
        
//         return "payment-summary";
//     }

//     @PostMapping("/pay-with-gomoney")
//     public String payWithGoMoney(@AuthenticationPrincipal UserDetails userDetails,
//                                  @RequestParam float cost,
//                                  @RequestParam String journeySummary,
//                                  RedirectAttributes redirectAttributes) {
        
//         userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//             Double newBalance = user.getGoMoneyBalance() - cost;
//             userService.updateUserBalance(user.getUsername(), newBalance);
//         });
        
//         redirectAttributes.addFlashAttribute("journeySummary", journeySummary);
//         return "redirect:/booking-confirmed";
//     }
    
//     @PostMapping("/process-booking")
//     public String processBooking(@RequestParam String journeyPlanJson,
//                                 @RequestParam Map<String, String> allParams,
//                                 @AuthenticationPrincipal UserDetails userDetails,
//                                 Model model) throws Exception {
        
//         ObjectMapper mapper = new ObjectMapper();
//         JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
        
//         List<Passenger> passengers = new ArrayList<>();
//         int passengerCount = 0;
        
//         for (String key : allParams.keySet()) {
//             if (key.startsWith("passengers[") && key.endsWith("].name")) {
//                 passengerCount++;
//             }
//         }
        
//         for (int i = 0; i < passengerCount; i++) {
//             String name = allParams.get("passengers[" + i + "].name");
//             String phone = allParams.get("passengers[" + i + "].phone");
//             if (name != null && !name.trim().isEmpty()) {
//                 passengers.add(new Passenger(name, phone));
//             }
//         }
        
//         try {
//             BookingService.BookingResult bookingResult = bookingService.bookCabsForJourney(plan, passengers);
            
//             if (bookingResult.success()) {
//                 String bookingId = "GC-" + System.currentTimeMillis();
//                 FinalBooking finalBooking = new FinalBooking(bookingId, plan, passengers);
                
//                 bookingService.saveBooking(finalBooking);
                
//                 // Deduct the amount from the user's wallet
//                 float totalCost = (plan.totalCost() * passengers.size()) * 1.18f;
//                 userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//                     double newBalance = user.getGoMoneyBalance() - totalCost;
//                     userService.updateUserBalance(user.getUsername(), newBalance);
//                 });


//                 model.addAttribute("booking", finalBooking);
//                 model.addAttribute("bookingResult", bookingResult);
//                 model.addAttribute("passengers", passengers);
                
//                 return "final-ticket";
//             } else {
//                 model.addAttribute("error", bookingResult.message());
//                 return "booking-error";
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             model.addAttribute("error", "Booking failed: " + e.getMessage());
//             return "booking-error";
//         }
//     }

//     @GetMapping("/booking-confirmed")
//     public String showConfirmationPage() {
//         return "booking-confirmed";
//     }
    
//     @GetMapping("/download-ticket/{bookingId}")
//     public ResponseEntity<byte[]> downloadTicket(@PathVariable String bookingId) {
//         try {
//             List<FinalBooking> allBookings = bookingService.getAllBookings();
//             FinalBooking booking = allBookings.stream()
//                 .filter(b -> b.bookingId().equals(bookingId))
//                 .findFirst()
//                 .orElse(null);
            
//             if (booking == null) {
//                 return ResponseEntity.notFound().build();
//             }
            
//             byte[] pdfBytes = pdfService.generateTicketPdf(booking, booking.passengers(), null);
            
//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_PDF);
//             headers.setContentDispositionFormData("attachment", "ticket-" + bookingId + ".pdf");
//             headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
//             return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
    
//     @PostMapping("/passenger-details")
//     public String showPassengerDetails(@RequestParam String journeyPlanJson, Model model) {
//         model.addAttribute("journeyPlanJson", journeyPlanJson);
//         return "passenger-details";
//     }
    
//     @PostMapping("/process-passengers")
//     public String processPassengers(@RequestParam String journeyPlanJson,
//                                    @RequestParam Map<String, String> allParams,
//                                    @AuthenticationPrincipal UserDetails userDetails,
//                                    Model model) throws Exception {
        
//         ObjectMapper mapper = new ObjectMapper();
//         JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
        
//         List<Passenger> passengers = new ArrayList<>();
//         int passengerCount = 0;
        
//         for (String key : allParams.keySet()) {
//             if (key.startsWith("passengers[") && key.endsWith("].name")) {
//                 passengerCount++;
//             }
//         }
        
//         for (int i = 0; i < passengerCount; i++) {
//             String name = allParams.get("passengers[" + i + "].name");
//             String phone = allParams.get("passengers[" + i + "].phone");
//             if (name != null && !name.trim().isEmpty()) {
//                 passengers.add(new Passenger(name, phone));
//             }
//         }
        
//         float baseCost = plan.totalCost();
//         float subtotal = baseCost * passengers.size();
//         float gst = subtotal * 0.18f;
//         float totalCost = subtotal + gst;
        
//         model.addAttribute("plan", plan);
//         try {
//             String planJson = mapper.writeValueAsString(plan);
//             model.addAttribute("planJson", planJson);
//         } catch (Exception ignored) {}
//         model.addAttribute("passengers", passengers);
//         model.addAttribute("baseCost", baseCost);
//         model.addAttribute("subtotal", subtotal);
//         model.addAttribute("gst", gst);
//         model.addAttribute("totalCost", totalCost);

//         if (userDetails != null) {
//             userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//                 model.addAttribute("currentBalance", user.getGoMoneyBalance());
//             });
//         }
        
//         return "payment-summary";
//     }
    
//     @GetMapping("/track-vehicle")
//     public String showTrackVehiclePage() {
//         return "tracking-page";
//     }

//     @PostMapping("/track-vehicle")
//     public String trackVehicle(@RequestParam String trackingId, Model model, RedirectAttributes redirectAttributes) {
//         TrackingInfo info = new TrackingService().getTrackingInfo(trackingId);

//         if (info != null) {
//             model.addAttribute("trackingInfo", info);
//         } else {
//             model.addAttribute("error", "Invalid or unknown tracking ID.");
//         }
//         return "tracking-page";
//     }
// }





























































































































package com.goconnect.cabservice.controller;

import java.util.ArrayList; // IOException add kiya
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goconnect.cabservice.model.FinalBooking;
import com.goconnect.cabservice.model.JourneyPlan;
import com.goconnect.cabservice.model.Passenger;
import com.goconnect.cabservice.model.TrackingInfo;
import com.goconnect.cabservice.service.BookingService;
import com.goconnect.cabservice.service.JourneyService;
import com.goconnect.cabservice.service.PdfService;
import com.goconnect.cabservice.service.TrackingService;
import com.goconnect.cabservice.service.UserService; // Yeh import zaroori hai

@Controller
public class JourneyController {

    @Autowired
    private JourneyService journeyService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PdfService pdfService;
    
    @Autowired
    private TrackingService trackingService; // @Autowired wala service use karna hai

    @GetMapping("/")
    public String showPlannerPage() {
        return "journey-planner";
    }

    @PostMapping("/plan-journey")
    public String planJourney(
            @RequestParam String passengerName,
            @RequestParam String passengerPhone,
            @RequestParam String startStreet, 
            @RequestParam String startLocality,
            @RequestParam String startCity, 
            @RequestParam String startState, 
            @RequestParam String startCountry,
            @RequestParam String destStreet, 
            @RequestParam String destLocality,
            @RequestParam String destCity, 
            @RequestParam String destState, 
            @RequestParam String destCountry,
            Model model) {
        
        JourneyPlan plan = journeyService.planJourney(passengerName, passengerPhone, startStreet, startCity, startState, startCountry, 
                                                      destStreet, destCity, destState, destCountry);
        
        model.addAttribute("plan", plan);
        try {
            String planJson = new ObjectMapper().writeValueAsString(plan);
            model.addAttribute("planJson", planJson);
        } catch (Exception ignored) {}
        return "journey-results";
    }
    
    @PostMapping("/proceed-to-payment")
    public String showPaymentPage(@RequestParam float totalCost, 
                                    @RequestParam String journeySummary, 
                                    @AuthenticationPrincipal UserDetails userDetails, 
                                    Model model) {
        
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("journeySummary", journeySummary);
        
        userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
            model.addAttribute("currentBalance", user.getGoMoneyBalance());
        });
        
        return "payment-summary";
    }

    @PostMapping("/pay-with-gomoney")
    public String payWithGoMoney(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam float cost,
                                    @RequestParam String journeySummary,
                                    RedirectAttributes redirectAttributes) {
        
        userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
            Double newBalance = user.getGoMoneyBalance() - cost;
            userService.updateUserBalance(user.getUsername(), newBalance);
        });
        
        redirectAttributes.addFlashAttribute("journeySummary", journeySummary);
        return "redirect:/booking-confirmed";
    }
    
    @PostMapping("/process-booking")
    public String processBooking(@RequestParam String journeyPlanJson,
                                    @RequestParam Map<String, String> allParams,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper();
        JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
        
        List<Passenger> passengers = new ArrayList<>();
        int passengerCount = 0;
        
        for (String key : allParams.keySet()) {
            if (key.startsWith("passengers[") && key.endsWith("].name")) {
                passengerCount++;
            }
        }
        
        for (int i = 0; i < passengerCount; i++) {
            String name = allParams.get("passengers[" + i + "].name");
            String phone = allParams.get("passengers[" + i + "].phone");
            if (name != null && !name.trim().isEmpty()) {
                passengers.add(new Passenger(name, phone));
            }
        }
        
        try {
            BookingService.BookingResult bookingResult = bookingService.bookCabsForJourney(plan, passengers);
            
            if (bookingResult.success()) {
                String bookingId = "GC-" + System.currentTimeMillis();
                FinalBooking finalBooking = new FinalBooking(bookingId, plan, passengers);
                
                bookingService.saveBooking(finalBooking);
                
                // Deduct the amount from the user's wallet
                float totalCost = (plan.totalCost() * passengers.size()) * 1.18f;
                userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                    double newBalance = user.getGoMoneyBalance() - totalCost;
                    userService.updateUserBalance(user.getUsername(), newBalance);
                });


                model.addAttribute("booking", finalBooking);
                model.addAttribute("bookingResult", bookingResult);
                model.addAttribute("passengers", passengers);
                
                return "final-ticket";
            } else {
                model.addAttribute("error", bookingResult.message());
                return "booking-error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Booking failed: " + e.getMessage());
            return "booking-error";
        }
    }

    @GetMapping("/booking-confirmed")
    public String showConfirmationPage() {
        return "booking-confirmed";
    }
    
    @GetMapping("/download-ticket/{bookingId}")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable String bookingId) {
        try {
            List<FinalBooking> allBookings = bookingService.getAllBookings();
            FinalBooking booking = allBookings.stream()
                .filter(b -> b.bookingId().equals(bookingId))
                .findFirst()
                .orElse(null);
            
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }
            
            // BookingResult ko null pass kar rahe hain, kyunki PDF banate waqt live cab data zaroori nahi hai
            byte[] pdfBytes = pdfService.generateTicketPdf(booking, booking.passengers(), null);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket-" + bookingId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/passenger-details")
    public String showPassengerDetails(@RequestParam String journeyPlanJson, Model model) {
        model.addAttribute("journeyPlanJson", journeyPlanJson);
        return "passenger-details";
    }
    
    @PostMapping("/process-passengers")
    public String processPassengers(@RequestParam String journeyPlanJson,
                                    @RequestParam Map<String, String> allParams,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper();
        JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
        
        List<Passenger> passengers = new ArrayList<>();
        int passengerCount = 0;
        
        for (String key : allParams.keySet()) {
            if (key.startsWith("passengers[") && key.endsWith("].name")) {
                passengerCount++;
            }
        }
        
        for (int i = 0; i < passengerCount; i++) {
            String name = allParams.get("passengers[" + i + "].name");
            String phone = allParams.get("passengers[" + i + "].phone");
            if (name != null && !name.trim().isEmpty()) {
                passengers.add(new Passenger(name, phone));
            }
        }
        
        float baseCost = plan.totalCost();
        float subtotal = baseCost * passengers.size();
        float gst = subtotal * 0.18f;
        float totalCost = subtotal + gst;
        
        model.addAttribute("plan", plan);
        try {
            String planJson = mapper.writeValueAsString(plan);
            model.addAttribute("planJson", planJson);
        } catch (Exception ignored) {}
        model.addAttribute("passengers", passengers);
        model.addAttribute("baseCost", baseCost);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("gst", gst);
        model.addAttribute("totalCost", totalCost);

        if (userDetails != null) {
            userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentBalance", user.getGoMoneyBalance());
            });
        }
        
        return "payment-summary";
    }
    
    // --- YEH AAPKA NAYA FIX HAI ---

    @GetMapping("/track-vehicle")
    public String showTrackVehiclePage() {
        return "tracking-page";
    }

    // Yeh function /track/booking/{bookingId} waale link ko handle karega
    @GetMapping("/track/booking/{bookingId}")
    public String trackBooking(@PathVariable String bookingId, Model model) {
        // @Autowired wala trackingService use karein
        TrackingInfo info = trackingService.getTrackingInfo(bookingId);

        if (info != null) {
            model.addAttribute("trackingInfo", info);
        } else {
            model.addAttribute("error", "Invalid or unknown booking ID.");
        }
        return "tracking-page";
    }

    // Yeh function "tracking-page.html" ke search form ko handle karega
    @PostMapping("/track-vehicle")
    public String trackVehicle(@RequestParam String trackingId, Model model) {
        // @Autowired wala trackingService use karein (new TrackingService() galat tha)
        TrackingInfo info = trackingService.getTrackingInfo(trackingId);

        if (info != null) {
            model.addAttribute("trackingInfo", info);
        } else {
            model.addAttribute("error", "Invalid or unknown tracking ID.");
        }
        return "tracking-page";
    }
}