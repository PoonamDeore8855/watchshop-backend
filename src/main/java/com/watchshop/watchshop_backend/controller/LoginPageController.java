//package com.watchshop.watchshop_backend.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import com.watchshop.watchshop_backend.entity.User;
//import com.watchshop.watchshop_backend.service.UserService;
//
//@Controller
//@RequestMapping("/auth")
//public class LoginPageController {
//
//    private final UserService userService;
//
//    public LoginPageController(UserService userService) {
//        this.userService = userService;
//    }
//
//    // ✅ OPEN LOGIN PAGE
//    @GetMapping("/login")
//    public String loginPage() {
//        return "redirect:/login.html";
//    }
//
//    // ✅ HANDLE LOGIN FORM
//    @PostMapping("/login-page")
//    @ResponseBody
//    public String loginSubmit(
//            @RequestParam String email,
//            @RequestParam String password) {
//
//        User user = userService.login(email, password);
//
//        if (user == null) {
//            return "❌ Invalid email or password";
//        }
//
//        return "✅ Login successful. Welcome " + user.getUsername();
//    }
//}
