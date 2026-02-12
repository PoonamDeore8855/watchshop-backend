package com.watchshop.watchshop_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.watchshop.watchshop_backend.dto.CheckoutRequestDTO;
import com.watchshop.watchshop_backend.dto.OrderResponseDTO;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.service.CheckoutService;
import com.watchshop.watchshop_backend.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
            @RequestBody CheckoutRequestDTO request,
            Authentication authentication
    ) {
        long startTime = System.currentTimeMillis();
        try {
            Order order = checkoutService.placeOrder(request, authentication);
            long orderSavedTime = System.currentTimeMillis();
            System.out.println("⏱️ Order Placement took: " + (orderSavedTime - startTime) + "ms");
            
            com.razorpay.Order rzpOrder = null;
            if ("ONLINE".equalsIgnoreCase(request.getPaymentMethod())) {
                rzpOrder = checkoutService.createRazorpayOrder(order);
                long rzpCreatedTime = System.currentTimeMillis();
                System.out.println("✅ Unified: Created Razorpay Order " + rzpOrder.get("id").toString() + " in " + (rzpCreatedTime - orderSavedTime) + "ms");
            }
            
            System.out.println("🚀 Total Processing Time: " + (System.currentTimeMillis() - startTime) + "ms");
            return ResponseEntity.ok(toDTO(order, rzpOrder));
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    // 🔥 THIS METHOD WAS MISSING
    private OrderResponseDTO toDTO(Order order) {
        return toDTO(order, null);
    }

    private OrderResponseDTO toDTO(Order order, com.razorpay.Order rzpOrder) {
        java.util.List<com.watchshop.watchshop_backend.dto.OrderItemResponseDTO> items = order.getOrderItems().stream()
                 .map(item -> new com.watchshop.watchshop_backend.dto.OrderItemResponseDTO(
                         item.getId(),
                         item.getProduct().getId(),
                         item.getProduct().getName(),
                         item.getQuantity(),
                         item.getPrice(),
                         item.getProduct().getImageUrl()
                 ))
                 .collect(java.util.stream.Collectors.toList());

        if (rzpOrder != null) {
            return new OrderResponseDTO(
                    order.getId(),
                    order.getTotalAmount(),
                    order.getUser().getId(),
                    order.getUser().getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    order.getDiscountAmount(),
                    order.getPromoCode(),
                    items,
                    order.getEmailError(),
                    rzpOrder.get("id") != null ? rzpOrder.get("id").toString() : null,
                    rzpOrder.get("amount") != null ? ((Number) rzpOrder.get("amount")).intValue() : 0,
                    rzpOrder.get("currency") != null ? rzpOrder.get("currency").toString() : "INR"
            );
        }

        return new OrderResponseDTO(
                order.getId(),
                order.getTotalAmount(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getOrderDate(),
                order.getDiscountAmount(),
                order.getPromoCode(),
                items,
                order.getEmailError()
        );
    }
}
