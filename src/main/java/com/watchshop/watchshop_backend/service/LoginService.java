package com.watchshop.watchshop_backend.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.watchshop.watchshop_backend.dto.LoginRequestDTO;
import com.watchshop.watchshop_backend.dto.LoginResponseDTO;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.UserRepository;
import com.watchshop.watchshop_backend.utils.JwtUtil;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // ❌ user not found
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        // ❌ Google account trying normal login
        if ("GOOGLE".equals(user.getProvider())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Please login using Google");
        }

        // ❌ password mismatch
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        // ✅ Generate JWT
        String token = jwtUtil.generateToken(
                user.getEmail()
        );

        LoginResponseDTO userDto =
                new LoginResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", userDto
        ));
    }
}
