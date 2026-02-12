package com.watchshop.watchshop_backend.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.entity.Order;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOrderConfirmation(String toEmail, Order order) {
        if (toEmail == null || toEmail.isBlank() || !toEmail.contains("@")) {
            System.err.println("❌ Invalid email address: " + toEmail + ". Skipping email dispatch.");
            return;
        }

        System.out.println("📧 Attempting to send order confirmation to: " + toEmail);

        String userName = (order.getUser() != null) ? order.getUser().getUsername() : "Customer";
        if (userName == null || userName.isBlank()) {
            userName = "Customer";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("🛍️ Order Confirmed | WatchShop");

        message.setText(
            "Hi " + userName + ",\n\n" +
            "Your order has been placed successfully 🎉\n\n" +
            "Order ID: " + order.getId() + "\n" +
            "Total Amount: ₹" + order.getTotalAmount() + "\n" +
            "Status: " + order.getStatus() + "\n\n" +
            "Thank you for shopping with us 😊\n\n" +
            "— WatchShop Team"
        );

        try {
            mailSender.send(message);
            System.out.println("✅ Order confirmation email sent successfully to " + toEmail);
        } catch (org.springframework.mail.MailException e) {
            String errorMsg = e.getMessage();
            System.err.println("❌ Mail delivery failed for " + toEmail + ": " + errorMsg);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during email dispatch: " + e.getMessage());
        }
    }
}
