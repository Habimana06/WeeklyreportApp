package com.example.weekly_report.controller;

import com.example.weekly_report.dto.AuthRequest;
import com.example.weekly_report.dto.AuthResponse;
import com.example.weekly_report.dto.UserCreateRequest;
import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.entity.UserRole;
import com.example.weekly_report.service.UserService;
import com.example.weekly_report.security.JwtService;
import com.example.weekly_report.service.TokenService;
import com.example.weekly_report.security.CustomUserDetailsService;
import com.example.weekly_report.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
                )
            );
            
            UserAccount user = (UserAccount) authentication.getPrincipal();
            // For 2FA, do not issue final token yet. Send a one-time code.
            String code = tokenService.createTwoFactorCode(user.getId(), 300);
            // Send code to user's email
            try {
                emailService.sendText(user.getEmail(), "Your verification code", "Your verification code is: " + code + "\nThis code expires in 5 minutes.");
            } catch (Exception ex) {
                System.out.println("Failed to send 2FA email: " + ex.getMessage());
            }
            
            AuthResponse response = new AuthResponse();
            response.setToken(null);
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            response.setEmail(user.getEmail());
            response.setId(user.getId());
            // Client will now call /auth/verify-2fa to obtain JWT
            
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactor(@RequestParam Long userId, @RequestParam String code) {
        try {
            // Validate code
            if (!tokenService.verifyTwoFactorCode(userId, code)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid or expired verification code");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            UserAccount user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            String token = jwtService.generateToken(user);
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            response.setEmail(user.getEmail());
            response.setId(user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            UserAccount user = userService.getByEmail(email)
                    .orElseThrow(() -> new RuntimeException("If the email exists, a link will be sent"));
            String token = tokenService.createPasswordResetToken(user.getUsername(), 3600);
            String link = "http://localhost:5173/reset-password?token=" + token;
            try {
                emailService.sendText(email, "Reset your password", "Click the link to reset your password: " + link + "\nIf you did not request this, ignore this email.");
            } catch (Exception ex) {
                System.out.println("Failed to send reset email: " + ex.getMessage());
            }
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "If the email exists, a reset link was sent.");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "If the email exists, a reset link was sent.");
            return ResponseEntity.ok(ok);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token");
            String password = body.get("password");
            String username = tokenService.consumePasswordResetToken(token);
            if (username == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            userService.updatePasswordByUsername(username, password);
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "Password updated");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        try {
            UserAccount user = userService.createUser(userCreateRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String username = jwtService.extractUsername(token);
            
            if (jwtService.isTokenValid(token, userDetailsService.loadUserByUsername(username))) {
                UserAccount user = (UserAccount) userDetailsService.loadUserByUsername(username);
                String newToken = jwtService.generateToken(user);
                
                AuthResponse response = new AuthResponse();
                response.setToken(newToken);
                response.setUsername(user.getUsername());
                response.setRole(user.getRole());
                response.setEmail(user.getEmail());
                response.setId(user.getId());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
