package com.watchshop.watchshop_backend.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.watchshop.watchshop_backend.dto.GoogleAuthRequest;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.UserRepository;
import com.watchshop.watchshop_backend.utils.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String googleClientId;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleAuthRequest request) {

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            // ✅ CORRECT: verify token string
            GoogleIdToken idToken = verifier.verify(request.getToken());

            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid Google token");
            }

            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 🔍 Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User u = new User();
                        u.setEmail(email);
                        u.setUsername(name);
                        u.setProvider("GOOGLE");
                        u.setRole("USER");
                        return userRepository.save(u);
                    });

            // 🔐 Generate JWT
            String jwt = jwtUtil.generateToken(user.getEmail());

            // ✅ Response
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Google login failed");
        }
    }
}
