package com.watchshop.watchshop_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.watchshop.watchshop_backend.dto.OrderItemRequestDTO;
import com.watchshop.watchshop_backend.dto.OrderItemResponseDTO;
import com.watchshop.watchshop_backend.service.OrderItemService;

@RestController
@RequestMapping("/order-items")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderItemController {

    private final OrderItemService service;

    public OrderItemController(OrderItemService service) {
        this.service = service;
    }

    // ✅ GET items by order id
    @GetMapping("/order/{orderId}")
    public List<OrderItemResponseDTO> getItemsByOrderId(
            @PathVariable Long orderId) {
        return service.getByOrderId(orderId);
    }

    // ✅ POST add item to order
    @PostMapping("/order/{orderId}")
    public OrderItemResponseDTO addItem(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequestDTO request) {
        return service.addItem(orderId, request);
    }

    // ✅ DELETE order item
    @DeleteMapping("/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        service.deleteItem(itemId);
        return "Order item deleted with id: " + itemId;
    }
}
