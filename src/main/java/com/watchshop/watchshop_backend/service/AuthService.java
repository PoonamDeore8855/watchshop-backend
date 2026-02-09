package com.watchshop.watchshop_backend.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.UserRepository;
import com.watchshop.watchshop_backend.utils.JwtUtil;

@Service
public class AuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> authenticate(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            new GsonFactory()
                    )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUsername(name);
                        newUser.setPassword("GOOGLE_USER");
                        newUser.setProvider("GOOGLE");
                        newUser.setRole("USER"); // ✅ VERY IMPORTANT
                        return userRepository.save(newUser);
                    });

            String jwt = jwtUtil.generateToken(
                    user.getEmail()
                    
            );


            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "email", user.getEmail(),
                    "username", user.getUsername()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Google authentication failed");
        }
    }
}
