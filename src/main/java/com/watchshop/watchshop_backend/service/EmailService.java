package com.watchshop.watchshop_backend.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.entity.Order;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendOrderConfirmation(String toEmail, Order order) {
        if (toEmail == null || toEmail.isBlank() || !toEmail.contains("@")) {
            String err = "Invalid email address: " + toEmail;
            System.err.println("❌ " + err + ". Skipping email dispatch.");
            return err;
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
            return null;
        } catch (org.springframework.mail.MailException e) {
            String errorMsg = e.getMessage();
            System.err.println("❌ Mail delivery failed for " + toEmail + ": " + errorMsg);
            
            String tip = "";
            if (errorMsg.contains("550")) {
                tip = " Verify if the email address exists.";
                System.err.println("💡 TIP: Verify if the email address exists and the SMTP server is configured correctly.");
            } else if (errorMsg.contains("552") || errorMsg.contains("5.2.2")) {
                tip = " Recipient inbox is full (552).";
                System.err.println("💡 TIP: The recipient's inbox is full (Error 552 5.2.2). Inform them to clear storage and then retry.");
            }
            return "Email failed: " + errorMsg + tip;
        } catch (Exception e) {
            String err = "Unexpected error during email dispatch: " + e.getMessage();
            System.err.println("❌ " + err);
            return err;
        }
    }
}
