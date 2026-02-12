package com.watchshop.watchshop_backend.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.Transaction;
import com.watchshop.watchshop_backend.repository.OrderRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;

import java.util.Map;
import java.util.HashMap;

@Service
public class PaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private InvoiceService invoiceService;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public com.razorpay.Order createRazorpayOrder(Double amount) throws Exception {
        JSONObject options = new JSONObject();
        // Razorpay accepts amount in paise (1 INR = 100 paise)
        options.put("amount", amount * 100);
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());
        options.put("payment_capture", 1); // Auto capture

        return razorpayClient.orders.create(options);
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getPaymentDetails(String paymentId) {
        try {
            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
            Map<String, Object> details = new HashMap<>();
            details.put("method", payment.get("method"));
            details.put("status", payment.get("status"));
            details.put("amount", payment.get("amount"));
            details.put("currency", payment.get("currency"));
            return details;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handle non-critical post-payment tasks asynchronously
     */
    @Async
    public void asyncPostPaymentProcessing(Order order, String razorpayOrderId, String razorpayPaymentId) {
        try {
            // 1. SAVE TRANSACTION
            System.out.println("Async processing transaction for Order ID: " + order.getId());
            Map<String, Object> paymentDetails = getPaymentDetails(razorpayPaymentId);
            String method = "UNKNOWN";
            
            if (paymentDetails != null && paymentDetails.containsKey("method")) {
                method = (String) paymentDetails.get("method");
            }
            
            Transaction transaction = new Transaction(
                order.getId(),
                order.getUser(),
                order.getTotalAmount(),
                "SUCCESS",
                method,
                razorpayPaymentId,
                razorpayOrderId
            );
            
            transactionService.saveTransaction(transaction);
            System.out.println("Successfully saved transaction in background for Order ID: " + order.getId());

            // 2. AUTO-GENERATE INVOICE
            invoiceService.generateInvoice(order);
            
        } catch (Exception e) {
            System.err.println("Async post-payment error for Order ID " + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
