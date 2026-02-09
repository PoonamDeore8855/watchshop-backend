package com.watchshop.watchshop_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.watchshop.watchshop_backend.dto.GoogleAuthRequest;
//import com.watchshop.watchshop_backend.dto.GoogleLoginRequest;
import com.watchshop.watchshop_backend.dto.LoginRequestDTO;
import com.watchshop.watchshop_backend.dto.RegisterRequestDTO;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;
import com.watchshop.watchshop_backend.service.AuthService;
import com.watchshop.watchshop_backend.service.LoginService;
import com.watchshop.watchshop_backend.service.RegisterService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;
    private final ProductRepository ProductRepository;
    public AuthController(AuthService authService,ProductRepository ProductRepository) {
        this.authService = authService;
		this.ProductRepository = ProductRepository;
    }
    
    @Autowired
    private RegisterService registerService;
    @Autowired
    private LoginService loginService;
    
    private UserRepository UserRepository;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        return registerService.register(request);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        return loginService.login(request);
    }

    @PostMapping(
        value = "/google",
        consumes = "application/json",
        produces = "application/json"
    )
    public ResponseEntity<?> googleLogin(
            @RequestBody GoogleAuthRequest request) {

        // ✅ FIXED LINE
        return authService.authenticate(request.getToken());
    }
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return ProductRepository.findAll();
    }
    @GetMapping("/users")
    public List<User> getAllUsers()
    {
    	return UserRepository.findAll();
    }
     
}
