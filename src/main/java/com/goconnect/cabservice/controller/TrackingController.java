package com.goconnect.cabservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.goconnect.cabservice.model.TrackingInfo;
import com.goconnect.cabservice.service.TrackingService;

@Controller
public class TrackingController {
    
    @Autowired
    private TrackingService trackingService;

    // Shows the main tracking dashboard
    @GetMapping("/track")
    public String showTrackingPage() {
        return "tracking-dashboard";
    }
    
    // Shows the new live tracking page
    @GetMapping("/tracking")
    public String showLiveTrackingPage() {
        return "tracking";
    }

    // Handles the tracking ID submission
    @PostMapping("/track")
    public String getTrackingInfo(@RequestParam String trackingId, Model model, RedirectAttributes redirectAttributes) {
        TrackingInfo info = trackingService.getTrackingInfo(trackingId);

        if (info != null) {
            model.addAttribute("info", info);
            return "tracking-results"; // Show the results page
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid ID format or not found. Please try again.");
            return "redirect:/track"; // Go back to the dashboard with an error
        }
    }
}
