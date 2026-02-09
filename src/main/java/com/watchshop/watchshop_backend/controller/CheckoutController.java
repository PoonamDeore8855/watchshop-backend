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
        try {
            Order order = checkoutService.placeOrder(request, authentication);
            return ResponseEntity.ok(toDTO(order));
        } catch (Exception e) {
            e.printStackTrace();
            // Return 400 so client knows it's a data issue (e.g. stale cart)
            return ResponseEntity.status(400).body("Order failed: " + e.getMessage());
        }
    }

    // 🔥 THIS METHOD WAS MISSING
    private OrderResponseDTO toDTO(Order order) {
        
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
