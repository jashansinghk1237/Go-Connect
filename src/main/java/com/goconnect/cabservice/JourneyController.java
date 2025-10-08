package com.goconnect.cabservice;

import java.util.ArrayList;
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

    @GetMapping("/")
    public String showPlannerPage() {
        return "journey-planner";
    }

    @PostMapping("/plan-journey")
    public String planJourney(
            // This parameter list now perfectly matches the HTML form
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
        
        // Parse journey plan
        ObjectMapper mapper = new ObjectMapper();
        JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
        
        // Extract passenger data
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
        
        // Try to book cabs
        try {
            BookingService.BookingResult bookingResult = bookingService.bookCabsForJourney(plan, passengers);
            
            if (bookingResult.success()) {
                // Create final booking
                String bookingId = "GC-" + System.currentTimeMillis();
                FinalBooking finalBooking = new FinalBooking(bookingId, plan, passengers);
                
                // Save booking
                bookingService.saveBooking(finalBooking);
                
                model.addAttribute("booking", finalBooking);
                model.addAttribute("bookingResult", bookingResult);
                model.addAttribute("passengers", passengers);
                
                return "final-ticket";
            } else {
                model.addAttribute("error", bookingResult.message());
                return "booking-error";
            }
        } catch (Exception e) {
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
            // Find the booking
            List<FinalBooking> allBookings = bookingService.getAllBookings();
            FinalBooking booking = allBookings.stream()
                .filter(b -> b.bookingId().equals(bookingId))
                .findFirst()
                .orElse(null);
            
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Generate PDF
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
        
        // Parse journey plan
        ObjectMapper mapper = new ObjectMapper();
        JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
        
        // Extract passenger data
        List<Passenger> passengers = new ArrayList<>();
        int passengerCount = 0;
        
        // Count passengers by looking for name fields
        for (String key : allParams.keySet()) {
            if (key.startsWith("passengers[") && key.endsWith("].name")) {
                passengerCount++;
            }
        }
        
        // Extract passenger details
        for (int i = 0; i < passengerCount; i++) {
            String name = allParams.get("passengers[" + i + "].name");
            String phone = allParams.get("passengers[" + i + "].phone");
            if (name != null && !name.trim().isEmpty()) {
                passengers.add(new Passenger(name, phone));
            }
        }
        
        // Calculate costs
        float baseCost = plan.totalCost();
        float subtotal = baseCost * passengers.size();
        float gst = subtotal * 0.18f;
        float totalCost = subtotal + gst;
        
        model.addAttribute("plan", plan);
        // Provide serialized JSON for form submission to /process-booking
        try {
            String planJson = mapper.writeValueAsString(plan);
            model.addAttribute("planJson", planJson);
        } catch (Exception ignored) {}
        model.addAttribute("passengers", passengers);
        model.addAttribute("baseCost", baseCost);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("gst", gst);
        model.addAttribute("totalCost", totalCost);

        // Add current wallet balance for the logged-in user
        if (userDetails != null) {
            userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentBalance", user.getGoMoneyBalance());
            });
        }
        
        return "payment-summary";
    }
}






































// package com.goconnect.cabservice;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// @Controller
// public class JourneyController {

//     @Autowired
//     private JourneyService journeyService;
    
//     @Autowired
//     private UserService userService;

//     @GetMapping("/")
//     public String showPlannerPage() {
//         return "journey-planner";
//     }

//     @PostMapping("/plan-journey")
//     public String planJourney(
//             @RequestParam String passengerName,
//             @RequestParam String passengerPhone,
//             @RequestParam String startStreet, 
//             @RequestParam String startLocality, // <-- This was missing
//             @RequestParam String startCity, 
//             @RequestParam String startState, 
//             @RequestParam String startCountry,
//             @RequestParam String destStreet, 
//             @RequestParam String destLocality, // <-- This was missing
//             @RequestParam String destCity, 
//             @RequestParam String destState, 
//             @RequestParam String destCountry,
//             Model model) {
        
//         // The service call is now correct, even though it doesn't use the locality fields yet.
//         JourneyPlan plan = journeyService.planJourney(passengerName, passengerPhone, startStreet, startCity, startState, startCountry, 
//                                                        destStreet, destCity, destState, destCountry);
        
//         model.addAttribute("plan", plan);
//         return "journey-results";
//     }
    
//     // ... all other methods (proceed-to-payment, pay-with-gomoney, etc.) remain the same ...
//     @PostMapping("/proceed-to-payment")
//     public String showPaymentPage(@RequestParam float totalCost, 
//                                   @RequestParam String journeySummary, 
//                                   @AuthenticationPrincipal UserDetails userDetails, 
//                                   Model model) {
        
//         model.addAttribute("totalCost", totalCost);
//         model.addAttribute("journeySummary", journeySummary);
        
//         userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//             model.addAttribute("currentBalance", user.goMoneyBalance());
//         });
        
//         return "payment-summary";
//     }

//     @PostMapping("/pay-with-gomoney")
//     public String payWithGoMoney(@AuthenticationPrincipal UserDetails userDetails,
//                                  @RequestParam float cost,
//                                  @RequestParam String journeySummary,
//                                  RedirectAttributes redirectAttributes) {
        
//         userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//             float newBalance = (float) (user.goMoneyBalance() - cost);
//             userService.updateUserBalance(user.username(), newBalance);
//         });
        
//         redirectAttributes.addFlashAttribute("journeySummary", journeySummary);
//         return "redirect:/booking-confirmed";
//     }

//     @GetMapping("/booking-confirmed")
//     public String showConfirmationPage() {
//         return "booking-confirmed";
//     }
// }











// package com.goconnect.cabservice;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// @Controller
// public class JourneyController {

//     @Autowired
//     private JourneyService journeyService;
    
//     @Autowired
//     private UserService userService;

//     @GetMapping("/")
//     public String showPlannerPage() {
//         return "journey-planner";
//     }

//     @PostMapping("/plan-journey")
//     public String planJourney(
//             @RequestParam String passengerName,
//             @RequestParam String passengerPhone, // <-- This parameter was missing
//             @RequestParam String startStreet, @RequestParam String startLocality, @RequestParam String startCity, @RequestParam String startState, @RequestParam String startCountry,
//             @RequestParam String destStreet, @RequestParam String destLocality, @RequestParam String destCity, @RequestParam String destState, @RequestParam String destCountry,
//             Model model) {
        
//         // THE FIX IS HERE: We now pass the 'passengerPhone' to the service
//         JourneyPlan plan = journeyService.planJourney(passengerName, passengerPhone, startStreet, startCity, startState, startCountry, 
//                                                        destStreet, destCity, destState, destCountry);
        
//         model.addAttribute("plan", plan);
//         return "journey-results";
//     }
    
//     // ... all other methods (proceed-to-payment, pay-with-gomoney, etc.) remain the same ...
//     @PostMapping("/proceed-to-payment")
//     public String showPaymentPage(@RequestParam float totalCost, 
//                                   @RequestParam String journeySummary, 
//                                   @AuthenticationPrincipal UserDetails userDetails, 
//                                   Model model) {
        
//         model.addAttribute("totalCost", totalCost);
//         model.addAttribute("journeySummary", journeySummary);
        
//         userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//             model.addAttribute("currentBalance", user.goMoneyBalance());
//         });
        
//         return "payment-summary";
//     }

//     @PostMapping("/pay-with-gomoney")
//     public String payWithGoMoney(@AuthenticationPrincipal UserDetails userDetails,
//                                  @RequestParam float cost,
//                                  @RequestParam String journeySummary,
//                                  RedirectAttributes redirectAttributes) {
        
//         userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
//             float newBalance = (float) (user.goMoneyBalance() - cost);
//             userService.updateUserBalance(user.username(), newBalance);
//         });
        
//         redirectAttributes.addFlashAttribute("journeySummary", journeySummary);
//         return "redirect:/booking-confirmed";
//     }

//     @GetMapping("/booking-confirmed")
//     public String showConfirmationPage() {
//         return "booking-confirmed";
//     }
// }










































// package com.goconnect.cabservice;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.fasterxml.jackson.databind.ObjectMapper;

// @Controller
// public class JourneyController {

//     // ... inside your JourneyController class ...

// @Autowired
// private TrackingService trackingService; // Inject the tracking service

// // This method takes the user from the review page to the passenger details page
// @PostMapping("/collect-passenger-details")
// public String showPassengerDetailsForm(@RequestParam String journeyPlanJson, Model model) {
//     model.addAttribute("journeyPlanJson", journeyPlanJson);
//     // You can also deserialize the summary to show it on the page
//     // ObjectMapper mapper = new ObjectMapper();
//     // JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);
//     // model.addAttribute("journeySummary", plan.startLocation() + " to " + plan.finalDestination());
//     return "passenger-details";
// }

// // This is the final, most complex method
// @PostMapping("/generate-bill")
// public String generateFinalBill(@RequestParam String journeyPlanJson, 
//                                 @RequestParam Map<String, String> allParams, 
//                                 Model model) throws Exception {
    
//     ObjectMapper mapper = new ObjectMapper();
//     // 1. Recreate the original journey plan
//     JourneyPlan plan = mapper.readValue(journeyPlanJson, JourneyPlan.class);

//     // 2. Parse passenger data from the form
//     List<Passenger> passengers = new ArrayList<>();
//     int passengerCount = allParams.keySet().stream().filter(k -> k.startsWith("passengers")).map(k -> Integer.parseInt(k.replaceAll("[^0-9]", ""))).max(Integer::compareTo).orElse(-1) + 1;
//     for (int i = 0; i < passengerCount; i++) {
//         passengers.add(new Passenger(allParams.get("passengers[" + i + "].name"), allParams.get("passengers[" + i + "].phone")));
//     }

//     // 3. Fetch LIVE data and create a new "live" plan
//     List<JourneyLeg> liveLegs = new ArrayList<>();
//     for (JourneyLeg leg : plan.legs()) {
//         TrackingInfo liveInfo = trackingService.getTrackingInfo(leg.vehicleId());
//         String estimatedDeparture = leg.estimatedDeparture(); // Default
//         if (liveInfo != null && !liveInfo.schedule().isEmpty()) {
//             estimatedDeparture = liveInfo.schedule().get(0).estimatedTime(); // Get live departure time
//         }
//         liveLegs.add(new JourneyLeg(leg.legNumber(), leg.transportType(), leg.vehicleId(), leg.from(), leg.to(), leg.details(), leg.price(), estimatedDeparture));
//     }
//     JourneyPlan livePlan = new JourneyPlan(plan.passengerName(), plan.passengerPhone(), plan.startLocation(), plan.finalDestination(), plan.totalCost(), liveLegs);

//     // 4. Create and save the final booking
//     String bookingId = "GC-" + System.currentTimeMillis();
//     FinalBooking finalBooking = new FinalBooking(bookingId, plan, passengers);
    
//     // (Add logic here to write 'finalBooking' to 'final-bookings.json')

//     model.addAttribute("booking", finalBooking);
//     model.addAttribute("liveJourneyPlan", livePlan);
    
//     return "final-ticket";
// }


//     @Autowired
//     private JourneyService journeyService;

//     @GetMapping("/")
//     public String showPlannerPage() {
//         return "journey-planner";
//     }

//     @PostMapping("/plan-journey")
//     public String planJourney(
//             @RequestParam String passengerName,
//             @RequestParam String startStreet, @RequestParam String startLocality, @RequestParam String startCity, @RequestParam String startState, @RequestParam String startCountry,
//             @RequestParam String destStreet, @RequestParam String destLocality, @RequestParam String destCity, @RequestParam String destState, @RequestParam String destCountry,
//             Model model) {
        
//         JourneyPlan plan = journeyService.planJourney(passengerName, startStreet, startCity, startState, startCountry, 
//                                                        destStreet, destCity, destState, destCountry);
        
//         model.addAttribute("plan", plan);
        
//         return "journey-results";
//     }
// }








































































































// package com.goconnect.cabservice;

// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;

// @Controller
// public class JourneyController {

//     // Shows the main journey planner page
//     @GetMapping("/")
//     public String showPlannerPage() {
//         return "journey-planner";
//     }

//     // Handles the form submission
//     @PostMapping("/plan-journey")
//     public String planJourney(String passengerName, String finalDestination) {
//         // In a real app, this is where you would call the JourneyService
//         // to calculate the route and save it.
//         System.out.println("Planning journey for " + passengerName + " to " + finalDestination);
        
//         // For now, we will just redirect to a success page (which we can create later)
//         // or back to the cab list.
//         return "redirect:/cabs?location=Ambala&status=planned";
//     }
// }
