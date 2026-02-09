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

    public CheckoutService( 
    		OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            AdminRepository adminRepository,
            EmailService emailService) {
    	
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.adminRepository = adminRepository;
        this.emailService = emailService;
    }

    public Order placeOrder(CheckoutRequestDTO request, Authentication auth) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

    	User user = null;
        String email = (auth != null) ? auth.getName() : "test@example.com"; // Default for testing if auth is null

        // 1. Try to find in User table
        user = userRepository.findByEmail(email).orElse(null);

        // 2. If not found, check if it's an Admin and create a User record if necessary
        if (user == null) {
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
            throw new RuntimeException("User not found and could not be identified as Admin");
        }


    	Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;

        for (CartItemDTO item : request.getItems()) {

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found ID: " + item.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());

            totalAmount += product.getPrice() * item.getQuantity();
            orderItems.add(orderItem);
        }

        double discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0;
        order.setDiscountAmount(discount);
        order.setPromoCode(request.getPromoCode());
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount - discount);

     // ✅ save first
        Order savedOrder = orderRepository.save(order);
        
        System.out.println("👤 User email: " + savedOrder.getUser().getEmail());

        // ✅ send email AFTER save
        String emailRes = emailService.sendOrderConfirmation(
                savedOrder.getUser().getEmail(),
                savedOrder
        );
        savedOrder.setEmailError(emailRes);

        // ✅ return at the END
        return savedOrder;

}
}
