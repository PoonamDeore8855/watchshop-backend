package com.watchshop.watchshop_backend.dto;

import java.time.LocalDateTime;

public class OrderResponseDTO {

    private Long orderId;
    private Double totalAmount;
    private Long userId;
    private String email;
    private String status;
    private LocalDateTime orderDate;
    private Double discountAmount;
    private String promoCode;
    private java.util.List<OrderItemResponseDTO> items;
    private String emailError;

    // ✅ EXACT CONSTRUCTOR (THIS FIXES ERROR)
    public OrderResponseDTO(
            Long orderId,
            Double totalAmount,
            Long userId,
            String email,
            String status,
            LocalDateTime orderDate,
            Double discountAmount,
            String promoCode,
            java.util.List<OrderItemResponseDTO> items,
            String emailError
    ) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.orderDate = orderDate;
        this.discountAmount = discountAmount;
        this.promoCode = promoCode;
        this.items = items;
        this.emailError = emailError;
    }

    // ===== GETTERS =====
    public Long getOrderId() {
        return orderId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public java.util.List<OrderItemResponseDTO> getItems() {
        return items;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public String getEmailError() {
        return emailError;
    }
}
