package com.watchshop.watchshop_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Double amount;
    private String paymentStatus; // SUCCESS, FAILED
    private String paymentMethod; // UPI, CARD, NETBANKING, etc.
    
    private String razorpayPaymentId;
    private String razorpayOrderId;
    
    private LocalDateTime transactionDate;

    // Default constructor for JPA
    public Transaction() {}

    public Transaction(Long orderId, User user, Double amount, String paymentStatus, String paymentMethod, String razorpayPaymentId, String razorpayOrderId) {
        this.orderId = orderId;
        this.user = user;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.transactionDate = LocalDateTime.now();
    }
}
