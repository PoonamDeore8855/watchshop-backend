package com.watchshop.watchshop_backend.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.watchshop.watchshop_backend.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
            || path.startsWith("/api/admin/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            String path = request.getServletPath();
            if (path.startsWith("/api/checkout") || path.startsWith("/api/payment")) {
                System.err.println("⚠️ JWT Filter - Authorization header missing or malformed for path: " + path);
            }
        } else {
            String token = authHeader.substring(7);
            try {
                String email = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token); // USER / ADMIN

                System.out.println("JWT Filter - Email: " + email + ", Role: " + role);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (role == null || role.isEmpty()) {
                        System.err.println("JWT Filter Error: Role is null or empty for user " + email);
                    } else {
                        List<SimpleGrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + role));

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("JWT Filter - Authentication set for " + email + " with role ROLE_" + role);
                    }
                }
            } catch (Exception e) {
                System.err.println("JWT Filter Error: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
