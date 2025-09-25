package com.example.weekly_report.dto;

import com.example.weekly_report.entity.UserRole;

public class AuthResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private UserRole role;
    private Long id;
    private Long expiresIn;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, String username, String email, UserRole role, Long id, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.id = id;
        this.expiresIn = expiresIn;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}