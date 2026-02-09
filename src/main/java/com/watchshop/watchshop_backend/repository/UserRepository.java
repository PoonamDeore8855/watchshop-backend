package com.watchshop.watchshop_backend.repository;

import java.util.Optional;   // ✅ REQUIRED

import org.springframework.data.jpa.repository.JpaRepository;

import com.watchshop.watchshop_backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ MUST EXIST for login
    
    
    Optional<User> findByResetToken(String resetToken);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    
    
    

}
