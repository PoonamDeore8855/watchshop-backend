package com.watchshop.watchshop_backend.repository;

	import java.util.Optional;
	import org.springframework.data.jpa.repository.JpaRepository;
	import com.watchshop.watchshop_backend.entity.Cart;
	import com.watchshop.watchshop_backend.entity.User;

	public interface CartRepository extends JpaRepository<Cart, Long> {
	    Optional<Cart> findByUser(User user);
	}
