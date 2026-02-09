package com.watchshop.watchshop_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.dto.CartItemDTO;
import com.watchshop.watchshop_backend.entity.*;
import com.watchshop.watchshop_backend.repository.*;

@Service
public class CartItemService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartItemService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ✅ ADD ITEM
 // ✅ ADD ITEM
    public CartItemDTO addItem(Long productId, int qty, Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setPrice(product.getPrice());
        } else {
            item.setQuantity(item.getQuantity() + qty);
        }

        CartItem saved = cartItemRepository.save(item);

        // ✅ CORRECT DTO (4 params only)
        return new CartItemDTO(
                product.getId(),
                product.getName(),
                saved.getQuantity(),
                saved.getPrice()
        );
    }


    // ✅ GET CART
    public List<CartItemDTO> getItems(Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart empty"));

        return cartItemRepository.findByCart(cart)
                .stream()
                .map(i -> new CartItemDTO(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice()
                ))
                .collect(Collectors.toList());
    }

    // ✅ REMOVE ITEM (SECURE)
    public void removeItem(Long itemId, Authentication auth) {

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getCart().getUser().getEmail().equals(auth.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.delete(item);
    }
}
