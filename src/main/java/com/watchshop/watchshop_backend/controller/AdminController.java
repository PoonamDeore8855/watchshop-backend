package com.watchshop.watchshop_backend.controller;

import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.OrderRepository;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;
import com.watchshop.watchshop_backend.dto.OrderResponseDTO;
import com.watchshop.watchshop_backend.dto.OrderItemResponseDTO;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.service.ProductService;

import com.watchshop.watchshop_backend.dto.AdminLoginRequestDTO;
import com.watchshop.watchshop_backend.entity.Admin;
import com.watchshop.watchshop_backend.repository.AdminRepository;
import com.watchshop.watchshop_backend.service.AdminAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final AdminAuthService adminAuthService;
    private final AdminRepository adminRepository;

    public AdminController(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, ProductService productService, AdminAuthService adminAuthService, AdminRepository adminRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.adminAuthService = adminAuthService;
        this.adminRepository = adminRepository;
    }

    // 🔐 ADMIN LOGIN (Public)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequestDTO request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    // 📊 GET STATS (Dashboard Home)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        
        // Calculate Total Revenue
        List<Order> orders = orderRepository.findAll();
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("totalProducts", totalProducts);
        stats.put("totalUsers", totalUsers);
        stats.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(stats);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        List<User> users = userRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        // Daily Users
        Map<String, Long> dailyUsers = users.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().toLocalDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().toLocalDate().format(formatter),
                        TreeMap::new,
                        Collectors.counting()
                ));

        // Daily Orders
        Map<String, Long> dailyOrders = orders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalDate().format(formatter),
                        TreeMap::new,
                        Collectors.counting()
                ));

        // Daily Revenue
        Map<String, Double> dailyRevenue = orders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalDate().format(formatter),
                        TreeMap::new,
                        Collectors.summingDouble(Order::getTotalAmount)
                ));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("dailyUsers", dailyUsers);
        analytics.put("dailyOrders", dailyOrders);
        analytics.put("dailyRevenue", dailyRevenue);

        return ResponseEntity.ok(analytics);
    }

    // 📦 GET ALL ORDERS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    // 🔄 UPDATE ORDER STATUS
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        String newStatus = statusMap.get("status");
        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus);
            orderRepository.save(order);
            return ResponseEntity.ok("Order status updated to " + newStatus);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 👤 GET USER ORDER HISTORY
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}/orders")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            List<Order> orders = orderRepository.findByUser(user);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 📦 UPDATE PRODUCT
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 📦 ADD PRODUCT (Multipart)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/products", consumes = "multipart/form-data")
    public ResponseEntity<Product> addProduct(
            @RequestParam String name,
            @RequestParam double price,
            @RequestParam String brand,
            @RequestParam MultipartFile image
    ) {
        Product product = productService.addProduct(name, price, brand, image);
        return ResponseEntity.ok(product);
    }

    // 👤 GET ALL USERS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 👑 GET ALL ADMINS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admins")
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    // ❌ DELETE USER
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // 🔄 UPDATE USER ROLE
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleMap) {
        String newRole = roleMap.get("role");
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            userRepository.save(user);
            return ResponseEntity.ok("User role updated to " + newRole);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ❌ DELETE ORDER
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.ok("Order deleted successfully");
    }

    // ❌ DELETE PRODUCT (Secure)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully from the boutique catalog");
    }

    // 🛠️ DATA CLEANUP (Fix corrupted URL for product 1)

    // Helper: Verify this matches OrderResponseDTO constructor
    private OrderResponseDTO toDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getProduct().getImageUrl()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getTotalAmount(),
                order.getUser().getId(),
                order.getUser().getEmail(), // Assuming User has getEmail()
                order.getStatus(),
                order.getOrderDate(),
                order.getDiscountAmount(),
                order.getPromoCode(),
                items,
                order.getEmailError()
        );
    }
}
