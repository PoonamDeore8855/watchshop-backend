package com.watchshop.watchshop_backend.utils;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 🔑 SIGNING KEY
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 🔐 GENERATE TOKEN (EMAIL + ROLE)
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)   // USER / ADMIN (NO ROLE_)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration*120))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔁 DEFAULT ROLE USER
    public String generateToken(String email) {
        return generateToken(email, "USER");
    }

    // 📤 EXTRACT EMAIL
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 📤 EXTRACT ROLE
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // 🔎 PARSE CLAIMS
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ TOKEN VALIDATION
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ⏳ EXPIRY CHECK (OPTIONAL)
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
}
