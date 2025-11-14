package com.goconnect.cabservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.goconnect.cabservice.service.UserService;

@Controller
public class WalletController {

    @Autowired
    private UserService userService;

    // Shows the wallet page with the user's current balance
    @GetMapping("/wallet")
    public String showWalletPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
            model.addAttribute("currentBalance", user.getGoMoneyBalance());
        });
        return "wallet";
    }

    // Redirects to the simulated UPI gateway to add funds
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam float amount, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("totalCost", amount);
        redirectAttributes.addFlashAttribute("journeySummary", "Adding Funds to Wallet");
        redirectAttributes.addFlashAttribute("successUrl", "/wallet/payment-success?amount=" + amount); 
        return "redirect:/upi-payment";
    }

    // This is called after the "successful" UPI payment to update the balance
    @GetMapping("/wallet/payment-success")
    public String paymentSuccess(@AuthenticationPrincipal UserDetails userDetails, @RequestParam float amount) {
        userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
            Double newBalance = user.getGoMoneyBalance() + amount;
            userService.updateUserBalance(user.getUsername(), newBalance);
        });
        return "redirect:/wallet?success";
    }
}

