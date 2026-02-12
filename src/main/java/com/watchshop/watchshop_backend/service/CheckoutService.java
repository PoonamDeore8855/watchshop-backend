package com.watchshop.watchshop_backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.dto.CartItemDTO;
import com.watchshop.watchshop_backend.dto.CheckoutRequestDTO;
import com.watchshop.watchshop_backend.entity.Address;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.OrderItem;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.AdminRepository;
import com.watchshop.watchshop_backend.repository.OrderRepository;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;
    private final EmailService emailService;
    private final PaymentService paymentService;

    public CheckoutService( 
    		OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            AdminRepository adminRepository,
            EmailService emailService,
            PaymentService paymentService) {
    	
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.adminRepository = adminRepository;
        this.emailService = emailService;
        this.paymentService = paymentService;
    }

    public Order placeOrder(CheckoutRequestDTO request, Authentication auth) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

    	User user = null;
        String email = (auth != null) ? auth.getName() : "test@example.com"; // Default for testing if auth is null

        // 1. Try to find in User table
        user = userRepository.findByEmail(email).orElse(null);
        if (user != null) System.out.println("✅ Found User record for: " + email);

        // 2. If not found, check if it's an Admin and create a User record if necessary
        if (user == null) {
            System.out.println("🔍 User record mismatch, checking Admin table for: " + email);
            var admin = adminRepository.findByEmail(email).orElse(null);
            if (admin != null) {
                user = new User();
                user.setEmail(admin.getEmail());
                user.setUsername("Admin_" + admin.getEmail().split("@")[0]);
                user.setRole("ADMIN");
                user.setPassword(admin.getPassword()); // Sync password or set a random one
                user = userRepository.save(user);
                System.out.println("✅ Created shadow User for Admin: " + email);
            }
        }

        if (user == null) {
            System.err.println("❌ Checkout Failed: User not found in User or Admin table for email: " + email);
            throw new RuntimeException("User not found and could not be identified as Admin");
        }


    	Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());

        // 3. Batch Fetch Products for Speed
        java.util.List<Long> productIds = request.getItems().stream()
                .map(com.watchshop.watchshop_backend.dto.CartItemDTO::getProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        System.out.println("📦 Checkout IDs: " + productIds);
        
        java.util.Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p, (existing, replacement) -> existing));

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;

        for (CartItemDTO item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found ID: " + item.getProductId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());

            totalAmount += product.getPrice() * item.getQuantity();
            orderItems.add(orderItem);
        }

        // Match Frontend logic: Subtotal + Shipping + Tax
        double discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0;
        double subtotalAfterDiscount = totalAmount - discount;
        double shipping = subtotalAfterDiscount > 5000 ? 0 : 200;
        double tax = Math.round(subtotalAfterDiscount * 0.18);
        double finalTotal = subtotalAfterDiscount + shipping + tax;

        order.setDiscountAmount(discount);
        order.setPromoCode(request.getPromoCode());
        order.setOrderItems(orderItems);
        order.setTotalAmount(finalTotal);

     // ✅ save first
        Order savedOrder = orderRepository.save(order);
        
        System.out.println("👤 User email: " + savedOrder.getUser().getEmail());

        // ✅ send email AFTER save (Async)
        emailService.sendOrderConfirmation(
                savedOrder.getUser().getEmail(),
                savedOrder
        );

        // ✅ return at the END
        return savedOrder;
    }

    public com.razorpay.Order createRazorpayOrder(Order order) throws Exception {
        return paymentService.createRazorpayOrder(order.getTotalAmount());
    }
}
