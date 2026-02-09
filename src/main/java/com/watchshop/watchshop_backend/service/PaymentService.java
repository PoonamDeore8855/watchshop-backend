package com.watchshop.watchshop_backend.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

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
}
