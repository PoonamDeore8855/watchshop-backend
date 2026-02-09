package com.watchshop.watchshop_backend.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.watchshop.watchshop_backend.entity.Admin;


public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    
}

