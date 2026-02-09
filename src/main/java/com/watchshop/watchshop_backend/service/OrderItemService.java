package com.watchshop.watchshop_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.dto.OrderItemRequestDTO;
import com.watchshop.watchshop_backend.dto.OrderItemResponseDTO;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.OrderItem;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.repository.OrderItemRepository;
import com.watchshop.watchshop_backend.repository.OrderRepository;
import com.watchshop.watchshop_backend.repository.ProductRepository;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderItemService(OrderItemRepository orderItemRepo,
                            OrderRepository orderRepo,
                            ProductRepository productRepo) {
        this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    // ✅ GET items by order id
    public List<OrderItemResponseDTO> getByOrderId(Long orderId) {
        return orderItemRepo.findByOrderId(orderId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADD item to order
    public OrderItemResponseDTO addItem(Long orderId, OrderItemRequestDTO request) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setPrice(product.getPrice());

        OrderItem saved = orderItemRepo.save(item);

        // update order total
        Double currentTotal = order.getTotalAmount();
        if (currentTotal == null) {
            currentTotal = 0.0;
        }

        order.setTotalAmount(
            currentTotal + product.getPrice() * request.getQuantity()
        );
        orderRepo.save(order);

        return toDTO(saved);
    }

    // ✅ DELETE order item
    public void deleteItem(Long itemId) {

        OrderItem item = orderItemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        Order order = item.getOrder();
        double itemTotal = item.getPrice() * item.getQuantity();

        orderItemRepo.delete(item);

        order.setTotalAmount(order.getTotalAmount() - itemTotal);
        orderRepo.save(order);
    }

    // 🔁 Entity → DTO
    private OrderItemResponseDTO toDTO(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getProduct().getImageUrl()
        );
    }
}
