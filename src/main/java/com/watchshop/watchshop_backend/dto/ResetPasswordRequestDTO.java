package com.watchshop.watchshop_backend.dto;

public class ResetPasswordRequestDTO {

    private String token;
    private String newPassword;

    // 🔹 No-args constructor (important for Spring)
    public ResetPasswordRequestDTO() {
    }

    // 🔹 Getters & Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
