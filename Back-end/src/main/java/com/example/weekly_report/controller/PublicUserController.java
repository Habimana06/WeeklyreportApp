package com.example.weekly_report.controller;

import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.entity.UserRole;
import com.example.weekly_report.service.UserService;
import com.example.weekly_report.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/users")
@CrossOrigin(origins = "*")
public class PublicUserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    // Public endpoint for any authenticated user to get users by role (for messaging purposes)
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role, Authentication authentication) {
        try {
            System.out.println("=== DEBUG: PublicUserController.getUsersByRole called with role: " + role);
            
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("=== DEBUG: User not authenticated");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(401).body(error);
            }
            
            UserAccount currentUser = (UserAccount) authentication.getPrincipal();
            System.out.println("=== DEBUG: Current user: " + currentUser.getUsername() + " with role: " + currentUser.getRole());
            
            // Allow any authenticated user to get users by role (for messaging purposes)
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<UserAccount> users = userService.getUsersByRole(userRole);
            System.out.println("=== DEBUG: Found " + users.size() + " users with role " + userRole);

            List<Map<String, Object>> userDtos = new ArrayList<>();
            for (UserAccount u : users) {
                try {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", u.getId());
                    dto.put("username", u.getUsername());
                    dto.put("email", u.getEmail());
                    dto.put("role", u.getRole().name());
                    dto.put("enabled", u.isEnabled());
                    userDtos.add(dto);
                } catch (Exception userEx) {
                    System.err.println("=== DEBUG: Error processing user " + u.getId() + ": " + userEx.getMessage());
                }
            }
            return ResponseEntity.ok(userDtos);
        } catch (IllegalArgumentException e) {
            System.err.println("=== DEBUG: IllegalArgumentException in getUsersByRole: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid role: " + role);
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            System.err.println("=== DEBUG: Exception in getUsersByRole: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Test endpoint to send a test notification (for debugging)
    @PostMapping("/test-notification")
    public ResponseEntity<?> sendTestNotification(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            System.out.println("=== DEBUG: sendTestNotification called");
            
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(401).body(error);
            }
            
            UserAccount currentUser = (UserAccount) authentication.getPrincipal();
            Long recipientId = Long.valueOf(request.get("recipientId").toString());
            String subject = (String) request.get("subject");
            String body = (String) request.get("body");
            
            System.out.println("=== DEBUG: Sending test notification from " + currentUser.getUsername() + " to " + recipientId);
            
            // Send test notification
            notificationService.send(
                currentUser.getId(), // sender
                recipientId, // recipient
                null, // no report ID
                subject,
                body,
                null // no parent
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Test notification sent successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== DEBUG: Exception in sendTestNotification: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
