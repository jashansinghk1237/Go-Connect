package com.goconnect.cabservice;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CabController {

    @Autowired
    private CabService cabService;

    @GetMapping("/cabs")
    public String showAvailableCabs(@RequestParam String location, Model model) {
        List<Cab> availableCabs = cabService.findAvailableCabs(location);
        model.addAttribute("cabs", availableCabs);
        return "cabs";
    }

    @GetMapping("/cab/{id}")
    public String showBookingPage(@PathVariable int id, Model model) {
        Optional<Cab> cabOptional = cabService.findCabById(id);
        
        if (cabOptional.isPresent()) {
            model.addAttribute("cab", cabOptional.get());
            return "booking";
        } else {
            return "redirect:/cabs?location=Ambala"; 
        }
    }
}
