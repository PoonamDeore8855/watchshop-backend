package com.watchshop.watchshop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.dto.AdminLoginRequestDTO;
import com.watchshop.watchshop_backend.dto.AdminLoginResponseDTO;
import com.watchshop.watchshop_backend.entity.Admin;
import com.watchshop.watchshop_backend.repository.AdminRepository;
import com.watchshop.watchshop_backend.utils.JwtUtil;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AdminLoginResponseDTO login(AdminLoginRequestDTO request) {

        // ✅ FIX 1: small 'a' adminRepository
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                admin.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // ✅ FIX 2: jwtUtil instance + ADMIN Role
        String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");

        return new AdminLoginResponseDTO(token, admin.getEmail());
    }
}
