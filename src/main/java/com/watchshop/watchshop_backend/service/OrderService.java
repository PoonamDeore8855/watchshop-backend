package com.watchshop.watchshop_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.dto.CheckoutRequestDTO;
import com.watchshop.watchshop_backend.dto.OrderCreateRequestDTO;
import com.watchshop.watchshop_backend.dto.OrderItemRequestDTO;
import com.watchshop.watchshop_backend.dto.OrderResponseDTO;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.OrderItem;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.OrderItemRepository;
import com.watchshop.watchshop_backend.repository.OrderRepository;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;

    public OrderService(OrderRepository orderRepo,
                        UserRepository userRepo,
                        ProductRepository productRepo,
                        OrderItemRepository orderItemRepo) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.orderItemRepo = orderItemRepo;
    }

    // ✅ SIMPLE ORDER (POST /orders)
    public OrderResponseDTO save(Order order) {

        if (order == null || order.getUser() == null || order.getUser().getId() == null) {
            throw new RuntimeException("User is required for order");
        }

        User user = userRepo.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        order.setUser(user);

        if (order.getTotalAmount() == null) {
            order.setTotalAmount(0.0);
        }

        Order saved = orderRepo.save(order);
        return toDTO(saved);
    }

    // ✅ ORDER WITH ITEMS (POST /orders/with-items)
    public OrderResponseDTO createOrderWithItems(OrderCreateRequestDTO request) {

        // 🔒 SAFETY CHECKS (MOST IMPORTANT)
        if (request == null) {
            throw new RuntimeException("Request body is missing");
        }

        if (request.getUserId() == null) {
            throw new RuntimeException("UserId is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order items are required");
        }

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(0.0);

        Order savedOrder = orderRepo.save(order);

        double total = 0.0;

        for (OrderItemRequestDTO itemDTO : request.getItems()) {

            if (itemDTO.getProductId() == null || itemDTO.getQuantity() == null) {
                throw new RuntimeException("ProductId and quantity are required");
            }

            Product product = productRepo.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(product.getPrice());

            orderItemRepo.save(item);

            total += product.getPrice() * itemDTO.getQuantity();
        }

        savedOrder.setTotalAmount(total);
        orderRepo.save(savedOrder);

        return toDTO(savedOrder);
    }

    // ✅ GET ALL ORDERS
    public List<OrderResponseDTO> getAll() {
        return orderRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 🔁 ENTITY → DTO
    private OrderResponseDTO toDTO(Order order) {
        
        List<com.watchshop.watchshop_backend.dto.OrderItemResponseDTO> items = order.getOrderItems().stream()
                .map(item -> new com.watchshop.watchshop_backend.dto.OrderItemResponseDTO(
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
                order.getUser().getEmail(),
                order.getStatus(),
                order.getOrderDate(),
                order.getDiscountAmount(),
                order.getPromoCode(),
                items,
                order.getEmailError()
        );
    }

	public List<OrderResponseDTO> getMyOrders(Authentication authentication) {
		if (authentication == null) {
			throw new RuntimeException("User not authenticated");
		}
		
		String email = authentication.getName();
		User user = userRepo.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		return orderRepo.findByUser(user).stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

    public OrderResponseDTO getById(Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toDTO(order);
    }






}
