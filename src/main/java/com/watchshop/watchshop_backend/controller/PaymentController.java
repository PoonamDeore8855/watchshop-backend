package com.watchshop.watchshop_backend.controller;

import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.repository.OrderRepository;
import com.watchshop.watchshop_backend.service.PaymentService;
import com.watchshop.watchshop_backend.service.InvoiceService;
import com.watchshop.watchshop_backend.service.TransactionService;
import com.watchshop.watchshop_backend.entity.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Create Razorpay Order
     * This generates an order ID from Razorpay which is required to open the checkout
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            Double amount = Double.valueOf(data.get("amount").toString());
            com.razorpay.Order razorpayOrder = paymentService.createRazorpayOrder(amount);
            
            return ResponseEntity.ok(Map.of(
                "orderId", razorpayOrder.get("id"),
                "amount", razorpayOrder.get("amount"),
                "currency", razorpayOrder.get("currency"),
                "key", razorpayOrder.get("entity")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create payment order", "message", e.getMessage()));
        }
    }

    /**
     * Verify Payment Signature
     * After successful payment, Razorpay sends signature which we verify for security
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
        try {
            String razorpayOrderId = data.get("razorpayOrderId");
            String razorpayPaymentId = data.get("razorpayPaymentId");
            String signature = data.get("razorpaySignature");
            Long backendOrderId = Long.valueOf(data.get("backendOrderId"));

            // Verify signature to ensure payment is genuine
            boolean isValid = paymentService.verifySignature(razorpayOrderId, razorpayPaymentId, signature);

            if (isValid) {
                // Update order with payment details
                Order order = orderRepository.findById(backendOrderId).orElse(null);
                if (order != null) {
                    order.setRazorpayOrderId(razorpayOrderId);
                    order.setRazorpayPaymentId(razorpayPaymentId);
                    order.setPaymentStatus("PAID");
                    order.setStatus("CONFIRMED");
                    orderRepository.save(order);
                    
                    // ✅ SAVE TRANSACTION
                    try {
                        System.out.println("Processing transaction for Order ID: " + backendOrderId);
                        Map<String, Object> paymentDetails = paymentService.getPaymentDetails(razorpayPaymentId);
                        String method = "UNKNOWN";
                        
                        if (paymentDetails != null && paymentDetails.containsKey("method")) {
                            method = (String) paymentDetails.get("method");
                        } else {
                            System.out.println("Warning: Payment details from Razorpay were null or did not contain method. Using 'UNKNOWN'.");
                        }
                        
                        Transaction transaction = new Transaction(
                            backendOrderId,
                            order.getUser(),
                            order.getTotalAmount(),
                            "SUCCESS",
                            method,
                            razorpayPaymentId,
                            razorpayOrderId
                        );
                        
                        Transaction saved = transactionService.saveTransaction(transaction);
                        System.out.println("Successfully saved transaction. ID: " + saved.getId() + " for User: " + (order.getUser() != null ? order.getUser().getEmail() : "NULL"));
                    } catch (Exception e) {
                        System.err.println("CRITICAL: Failed to save transaction record: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // ✅ AUTO-GENERATE INVOICE
                    try {
                        invoiceService.generateInvoice(order);
                    } catch (Exception e) {
                        // Log error but don't fail payment verification
                        System.err.println("Failed to generate invoice: " + e.getMessage());
                    }
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Payment verified successfully",
                        "orderId", backendOrderId
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid payment signature"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Payment verification failed", "message", e.getMessage()));
        }
    }
}
