package com.watchshop.watchshop_backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.watchshop.watchshop_backend.dto.CartItemDTO;
import com.watchshop.watchshop_backend.dto.CartItemRequest;
import com.watchshop.watchshop_backend.service.CartItemService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private  CartItemService cartItemService;

    // ✅ ADD TO CART (JSON BODY)
    @PostMapping("/add")
    public CartItemDTO addToCart(@RequestBody CartItemRequest request, Authentication authentication) 
    {
        System.out.println("AUTH = " + authentication);
        return cartItemService.addItem(
                request.getProductId(),
                request.getQuantity(),
                authentication
        );
    }

    // ✅ GET LOGGED-IN USER CART
    @GetMapping
    public List<CartItemDTO> getMyCart(Authentication authentication) 
    {
        return cartItemService.getItems(authentication);
    }

    // ✅ REMOVE ITEM
    @DeleteMapping("/{itemId}")
    public void removeItem(@PathVariable Long itemId, Authentication authentication) 
    {
        cartItemService.removeItem(itemId, authentication);
    }
}
