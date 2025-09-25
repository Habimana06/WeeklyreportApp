package com.example.weekly_report.controller;

import com.example.weekly_report.dto.AssignSupervisorRequest;
import com.example.weekly_report.dto.UserCreateRequest;
import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.entity.UserRole;
import com.example.weekly_report.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            List<UserAccount> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId, Authentication authentication) {
        try {
            Optional<UserAccount> user = userService.getUserById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role, Authentication authentication) {
        try {
            System.out.println("=== DEBUG: getUsersByRole called with role: " + role);
            
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            System.out.println("=== DEBUG: Parsed role: " + userRole);
            
            List<UserAccount> users = userService.getUsersByRole(userRole);
            System.out.println("=== DEBUG: Found " + users.size() + " users with role " + userRole);
            
            // Create simple DTOs to avoid serialization issues
            List<Map<String, Object>> userDtos = new ArrayList<>();
            for (UserAccount user : users) {
                try {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", user.getId());
                    dto.put("username", user.getUsername());
                    dto.put("email", user.getEmail());
                    dto.put("role", user.getRole().name());
                    dto.put("enabled", user.isEnabled());
                    userDtos.add(dto);
                } catch (Exception userEx) {
                    System.err.println("=== DEBUG: Error processing user " + user.getId() + ": " + userEx.getMessage());
                }
            }
            
            return ResponseEntity.ok(userDtos);
        } catch (IllegalArgumentException e) {
            System.err.println("=== DEBUG: IllegalArgumentException in getUsersByRole: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid role: " + role);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("=== DEBUG: Exception in getUsersByRole: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request, 
                                       Authentication authentication) {
        try {
            UserAccount user = userService.createUser(request);
            
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
    
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                       @Valid @RequestBody UserCreateRequest request,
                                       Authentication authentication) {
        try {
            UserAccount user = userService.updateUser(userId, request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, Authentication authentication) {
        try {
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long userId, Authentication authentication) {
        try {
            userService.enableUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User enabled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long userId, Authentication authentication) {
        try {
            userService.disableUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User disabled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    // Add this test endpoint to your AdminController class
@GetMapping("/test/users")
public ResponseEntity<?> testGetUsers(Authentication authentication) {
    try {
        System.out.println("=== DEBUG: Test endpoint called");
        
        // Test 1: Get all users
        List<UserAccount> allUsers = userService.getAllUsers();
        System.out.println("=== DEBUG: Total users: " + allUsers.size());
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", allUsers.size());
        
        // Test 2: Count by role manually
        Map<String, Integer> roleCounts = new HashMap<>();
        for (UserAccount user : allUsers) {
            String roleName = user.getRole() != null ? user.getRole().name() : "NULL";
            roleCounts.put(roleName, roleCounts.getOrDefault(roleName, 0) + 1);
            System.out.println("=== DEBUG: User " + user.getUsername() + " has role: " + roleName);
        }
        
        response.put("roleCounts", roleCounts);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.err.println("=== DEBUG: Error in test endpoint: " + e.getMessage());
        e.printStackTrace();
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            System.out.println("=== DEBUG: Health check called");
            
            // Test basic database connectivity
            List<UserAccount> allUsers = userService.getAllUsers();
            System.out.println("=== DEBUG: Health check - Database connection OK, found " + allUsers.size() + " users");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("database", "Connected");
            response.put("totalUsers", allUsers.size());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== DEBUG: Health check failed: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/assign-supervisor")
    public ResponseEntity<Map<String, String>> assignSupervisor(@Valid @RequestBody AssignSupervisorRequest request,
                                                               Authentication authentication) {
        try {
            userService.assignSupervisor(request.getEmployeeId(), request.getSupervisorId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Supervisor assigned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Get user counts by role
            List<UserAccount> allUsers = userService.getAllUsers();
            long adminCount = allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN).count();
            long supervisorCount = allUsers.stream().filter(u -> u.getRole() == UserRole.SUPERVISOR).count();
            long employeeCount = allUsers.stream().filter(u -> u.getRole() == UserRole.EMPLOYEE).count();
            
            dashboard.put("totalUsers", allUsers.size());
            dashboard.put("adminCount", adminCount);
            dashboard.put("supervisorCount", supervisorCount);
            dashboard.put("employeeCount", employeeCount);
            dashboard.put("activeUsers", allUsers.stream().filter(UserAccount::isEnabled).count());
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/supervisors/{supervisorId}/employees")
    public ResponseEntity<?> getAssignedEmployees(@PathVariable Long supervisorId, Authentication authentication) {
        try {
            System.out.println("=== DEBUG: getAssignedEmployees called with supervisorId: " + supervisorId);
            
            // Check if user is admin or the supervisor themselves
            UserAccount currentUser = (UserAccount) authentication.getPrincipal();
            if (currentUser.getRole() != UserRole.ADMIN && !currentUser.getId().equals(supervisorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
            }
            
            List<Map<String, Object>> employeeDtos = userService.getAssignedEmployees(supervisorId);
            System.out.println("=== DEBUG: Found " + employeeDtos.size() + " assigned employees");
            
            return ResponseEntity.ok(employeeDtos);
        } catch (Exception e) {
            System.err.println("=== DEBUG: Exception in getAssignedEmployees: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


}
