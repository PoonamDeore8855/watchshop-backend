package com.watchshop.watchshop_backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.watchshop.watchshop_backend.entity.Cart;
import com.watchshop.watchshop_backend.entity.CartItem;
import com.watchshop.watchshop_backend.entity.Product;
   
    
    
    public interface CartItemRepository extends JpaRepository<CartItem, Long> {
        Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
        List<CartItem> findByCart(Cart cart);
    }
    
    
    
  
