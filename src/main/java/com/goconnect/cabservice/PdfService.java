package com.goconnect.cabservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

@Service
public class PdfService {
    
    public byte[] generateTicketPdf(FinalBooking booking, List<Passenger> passengers, BookingService.BookingResult bookingResult) throws IOException {
        String html = generateTicketHtml(booking, passengers, bookingResult);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        
        HtmlConverter.convertToPdf(html, writer);
        
        byte[] pdfBytes = outputStream.toByteArray();
        outputStream.close();
        
        return pdfBytes;
    }
    
    private String generateTicketHtml(FinalBooking booking, List<Passenger> passengers, BookingService.BookingResult bookingResult) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        html.append(".ticket { background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; border-bottom: 3px solid #10b981; padding-bottom: 20px; margin-bottom: 30px; }");
        html.append(".logo { font-size: 28px; font-weight: bold; color: #10b981; }");
        html.append(".booking-id { font-size: 18px; color: #6b7280; margin-top: 10px; }");
        html.append(".section { margin-bottom: 25px; }");
        html.append(".section-title { font-size: 18px; font-weight: bold; color: #1f2937; margin-bottom: 15px; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; }");
        html.append(".journey-leg { background: #f9fafb; padding: 15px; margin: 10px 0; border-radius: 8px; border-left: 4px solid #10b981; }");
        html.append(".cab-booking { background: #f0f9ff; padding: 15px; margin: 10px 0; border-radius: 8px; border-left: 4px solid #3b82f6; }");
        html.append(".passenger { background: #fef3c7; padding: 10px; margin: 5px 0; border-radius: 5px; }");
        html.append(".cost-summary { background: #f0fdf4; padding: 20px; border-radius: 8px; border: 2px solid #10b981; }");
        html.append(".total { font-size: 20px; font-weight: bold; color: #10b981; }");
        html.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 2px solid #e5e7eb; color: #6b7280; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<div class='ticket'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>GoConnect</div>");
        html.append("<div class='booking-id'>Booking ID: ").append(booking.bookingId()).append("</div>");
        html.append("<div style='margin-top: 10px; color: #6b7280;'>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))).append("</div>");
        html.append("</div>");
        
        // Journey Details
        html.append("<div class='section'>");
        html.append("<div class='section-title'>Journey Details</div>");
        html.append("<div style='font-size: 16px; margin-bottom: 15px;'>");
        html.append("<strong>From:</strong> ").append(booking.journeyPlan().startLocation()).append("<br>");
        html.append("<strong>To:</strong> ").append(booking.journeyPlan().finalDestination()).append("<br>");
        html.append("</div>");
        
        for (JourneyLeg leg : booking.journeyPlan().legs()) {
            html.append("<div class='journey-leg'>");
            html.append("<strong>Leg ").append(leg.legNumber()).append(": ").append(leg.transportType()).append("</strong><br>");
            html.append(leg.from()).append(" → ").append(leg.to()).append("<br>");
            html.append("<strong>Price:</strong> ₹").append(leg.price());
            html.append("</div>");
        }
        html.append("</div>");
        
        // Cab Bookings
        if (bookingResult.cabBookings() != null && !bookingResult.cabBookings().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>Cab Bookings</div>");
            
            for (BookingService.BookingResult.CabBooking cabBooking : bookingResult.cabBookings()) {
                html.append("<div class='cab-booking'>");
                html.append("<strong>Driver:</strong> ").append(cabBooking.driverName()).append("<br>");
                html.append("<strong>Phone:</strong> ").append(cabBooking.driverPhone()).append("<br>");
                html.append("<strong>Vehicle:</strong> ").append(cabBooking.licensePlate()).append("<br>");
                html.append("<strong>Route:</strong> ").append(cabBooking.from()).append(" → ").append(cabBooking.to()).append("<br>");
                html.append("<strong>Price:</strong> ₹").append(cabBooking.price());
                html.append("</div>");
            }
            html.append("</div>");
        }
        
        // Passengers
        html.append("<div class='section'>");
        html.append("<div class='section-title'>Passengers</div>");
        
        for (int i = 0; i < passengers.size(); i++) {
            Passenger passenger = passengers.get(i);
            html.append("<div class='passenger'>");
            html.append("<strong>").append(i + 1).append(". ").append(passenger.name()).append("</strong><br>");
            html.append("Phone: ").append(passenger.phone());
            html.append("</div>");
        }
        html.append("</div>");
        
        // Cost Summary
        html.append("<div class='section'>");
        html.append("<div class='section-title'>Cost Summary</div>");
        html.append("<div class='cost-summary'>");
        
        float baseCost = booking.journeyPlan().totalCost();
        int passengerCount = passengers.size();
        float subtotal = baseCost * passengerCount;
        float gst = subtotal * 0.18f;
        float total = subtotal + gst;
        
        html.append("<div style='margin-bottom: 10px;'>Base Cost per Person: ₹").append(String.format("%.2f", baseCost)).append("</div>");
        html.append("<div style='margin-bottom: 10px;'>Number of Passengers: ").append(passengerCount).append("</div>");
        html.append("<div style='margin-bottom: 10px;'>Subtotal: ₹").append(String.format("%.2f", subtotal)).append("</div>");
        html.append("<div style='margin-bottom: 10px;'>GST (18%): ₹").append(String.format("%.2f", gst)).append("</div>");
        html.append("<div style='border-top: 2px solid #10b981; padding-top: 10px; margin-top: 15px;'>");
        html.append("<div class='total'>Total Paid: ₹").append(String.format("%.2f", total)).append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p><strong>Important Information:</strong></p>");
        html.append("<p>• Please arrive at the pickup location 10 minutes before the scheduled time</p>");
        html.append("<p>• Keep this ticket handy for reference</p>");
        html.append("<p>• Contact the driver directly using the provided phone number</p>");
        html.append("<p>• In case of any issues, contact our customer support</p>");
        html.append("<p style='margin-top: 20px; font-size: 12px;'>Thank you for choosing GoConnect!</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
}
