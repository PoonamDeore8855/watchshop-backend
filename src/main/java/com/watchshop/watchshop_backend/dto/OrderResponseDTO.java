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
    private String razorpayOrderId;
    private Integer razorpayAmount;
    private String razorpayCurrency;

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
    
    // Additional constructor for Razorpay
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
            String emailError,
            String razorpayOrderId,
            Integer razorpayAmount,
            String razorpayCurrency
    ) {
        this(orderId, totalAmount, userId, email, status, orderDate, discountAmount, promoCode, items, emailError);
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayAmount = razorpayAmount;
        this.razorpayCurrency = razorpayCurrency;
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

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public Integer getRazorpayAmount() {
        return razorpayAmount;
    }

    public String getRazorpayCurrency() {
        return razorpayCurrency;
    }
}
