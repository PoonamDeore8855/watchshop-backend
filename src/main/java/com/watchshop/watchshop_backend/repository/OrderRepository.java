package com.watchshop.watchshop_backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
