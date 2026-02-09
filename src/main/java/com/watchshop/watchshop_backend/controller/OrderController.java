package com.watchshop.watchshop_backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.watchshop.watchshop_backend.dto.OrderResponseDTO;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // ✅ CREATE ORDER
    @PostMapping
    public OrderResponseDTO createOrder(
            @RequestBody Order order,
            Authentication authentication
    ) {
        return service.save(order);
    }

    // ✅ GET LOGGED-IN USER ORDERS
    @GetMapping("/my")
    public List<OrderResponseDTO> myOrders(Authentication authentication) {
        return service.getMyOrders(authentication);
    }

    // ✅ GET SINGLE ORDER
    @GetMapping("/{id}")
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        return service.getById(id);
    }
}
