package com.watchshop.watchshop_backend.dto;

public class AdminLoginResponseDTO {

    private String token;
    private String email;

    public AdminLoginResponseDTO(String token, String email) {
        this.token = token;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }
}
