package com.example.weekly_report.service;

import com.example.weekly_report.dto.UserCreateRequest;
import com.example.weekly_report.entity.*;
import com.example.weekly_report.repository.UserAccountRepository;
import com.example.weekly_report.repository.AdminProfileRepository;
import com.example.weekly_report.repository.EmployeeProfileRepository;
import com.example.weekly_report.repository.SupervisorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserAccountRepository userAccountRepository;
    
    @Autowired
    private AdminProfileRepository adminProfileRepository;
    
    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;
    
    @Autowired
    private SupervisorProfileRepository supervisorProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private NotificationService notificationService;

    
    public UserAccount createUser(UserCreateRequest request) {
        // Check if username already exists
        if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userAccountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create user account
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername());
        userAccount.setEmail(request.getEmail());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setRole(request.getRole());
        userAccount.setEnabled(true);
        
        userAccount = userAccountRepository.save(userAccount);
        
        // Create profile based on role
        BaseProfile profile = createProfile(request, userAccount);
        userAccount.setProfile(profile);
        
        return userAccountRepository.save(userAccount);
    }
    
    private BaseProfile createProfile(UserCreateRequest request, UserAccount userAccount) {
        BaseProfile profile;
        
        switch (request.getRole()) {
            case ADMIN:
                AdminProfile adminProfile = new AdminProfile();
                adminProfile.setFirstName(request.getFirstName());
                adminProfile.setLastName(request.getLastName());
                adminProfile.setDepartment(request.getDepartment());
                adminProfile.setPosition(request.getPosition());
                adminProfile.setPhoneNumber(request.getPhoneNumber());
                adminProfile.setAdminId(generateAdminId());
                adminProfile.setUserAccount(userAccount);
                profile = adminProfileRepository.save(adminProfile);
                break;
                
            case SUPERVISOR:
                SupervisorProfile supervisorProfile = new SupervisorProfile();
                supervisorProfile.setFirstName(request.getFirstName());
                supervisorProfile.setLastName(request.getLastName());
                supervisorProfile.setDepartment(request.getDepartment());
                supervisorProfile.setPosition(request.getPosition());
                supervisorProfile.setPhoneNumber(request.getPhoneNumber());
                supervisorProfile.setSupervisorId(generateSupervisorId());
                supervisorProfile.setUserAccount(userAccount);
                profile = supervisorProfileRepository.save(supervisorProfile);
                break;
                
            case EMPLOYEE:
                EmployeeProfile employeeProfile = new EmployeeProfile();
                employeeProfile.setFirstName(request.getFirstName());
                employeeProfile.setLastName(request.getLastName());
                employeeProfile.setDepartment(request.getDepartment());
                employeeProfile.setPosition(request.getPosition());
                employeeProfile.setPhoneNumber(request.getPhoneNumber());
                employeeProfile.setEmployeeId(generateEmployeeId());
                employeeProfile.setUserAccount(userAccount);
                profile = employeeProfileRepository.save(employeeProfile);
                break;
                
            default:
                throw new RuntimeException("Invalid user role");
        }
        
        return profile;
    }
    
    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }
    
    public Optional<UserAccount> getUserById(Long id) {
        return userAccountRepository.findById(id);
    }
    
    public Optional<UserAccount> getUserByUsername(String username) {
        return userAccountRepository.findByUsername(username);
    }
    
    public Optional<UserAccount> getByEmail(String email) {
        return userAccountRepository.findByEmail(email);
    }
    
    public void updatePasswordByUsername(String username, String rawPassword) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String encoded = passwordEncoder.encode(rawPassword);
        // prevent reuse of the same password hash (best effort; BCrypt produces different hashes)
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from the old password");
        }
        user.setPassword(encoded);
        userAccountRepository.save(user);
    }
    
    public UserAccount updateUser(Long id, UserCreateRequest request) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update basic info
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Update profile
        BaseProfile profile = user.getProfile();
        if (profile != null) {
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setDepartment(request.getDepartment());
            profile.setPosition(request.getPosition());
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        
        return userAccountRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Safeguard: Do not allow deleting ADMIN accounts
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot delete an admin account");
        }

        userAccountRepository.delete(user);
    }
    
    public void enableUser(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userAccountRepository.save(user);
    }
    
    public void disableUser(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        userAccountRepository.save(user);
    }
    
    private String generateAdminId() {
        return "ADM" + System.currentTimeMillis();
    }
    
    private String generateSupervisorId() {
        return "SUP" + System.currentTimeMillis();
    }
    
    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }
    
    public void assignSupervisor(Long employeeId, Long supervisorId) {
        // Get the employee profile
        UserAccount employeeAccount = userAccountRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        if (employeeAccount.getRole() != UserRole.EMPLOYEE) {
            throw new RuntimeException("User is not an employee");
        }
        
        EmployeeProfile employeeProfile = (EmployeeProfile) employeeAccount.getProfile();
        if (employeeProfile == null) {
            throw new RuntimeException("Employee profile not found");
        }
        
        // Get the supervisor profile
        UserAccount supervisorAccount = userAccountRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        
        if (supervisorAccount.getRole() != UserRole.SUPERVISOR) {
            throw new RuntimeException("User is not a supervisor");
        }
        
        SupervisorProfile supervisorProfile = (SupervisorProfile) supervisorAccount.getProfile();
        if (supervisorProfile == null) {
            throw new RuntimeException("Supervisor profile not found");
        }
        
        // Check if supervisor has reached max employees limit
        if (supervisorProfile.getMaxEmployees() != null && 
            supervisorProfile.getSupervisedEmployees().size() >= supervisorProfile.getMaxEmployees()) {
            throw new RuntimeException("Supervisor has reached maximum employee limit");
        }
        
        // Assign supervisor to employee
        employeeProfile.setSupervisor(supervisorProfile);
        employeeProfileRepository.save(employeeProfile);
        
        // Send notification to employee
        try {
            notificationService.send(
                supervisorId, // sender (admin)
                employeeId, // recipient (employee)
                null, // no report ID
                "Supervisor Assigned",
                String.format("You have been assigned to supervisor %s %s", 
                    supervisorProfile.getFirstName(), supervisorProfile.getLastName()),
                null // no parent notification
            );
        } catch (Exception e) {
            // Log error but don't fail the supervisor assignment
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        // Send notification to supervisor
        try {
            notificationService.send(
                employeeId, // sender (admin)
                supervisorId, // recipient (supervisor)
                null, // no report ID
                "New Employee Assigned",
                String.format("Employee %s %s has been assigned to you", 
                    employeeProfile.getFirstName(), employeeProfile.getLastName()),
                null // no parent notification
            );
        } catch (Exception e) {
            // Log error but don't fail the supervisor assignment
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
    
    public List<UserAccount> getUsersByRole(UserRole role) {
        try {
            System.out.println("=== DEBUG: UserService.getUsersByRole called with role: " + role);
            List<UserAccount> users = userAccountRepository.findByRole(role);
            System.out.println("=== DEBUG: Repository returned " + users.size() + " users");
            return users;
        } catch (Exception e) {
            System.err.println("=== DEBUG: Exception in UserService.getUsersByRole: " + e.getMessage());
            System.err.println("=== DEBUG: Exception type: " + e.getClass().getName());
            e.printStackTrace();
            
            // Check for specific database errors
            if (e.getCause() != null) {
                System.err.println("=== DEBUG: Caused by: " + e.getCause().getMessage());
                System.err.println("=== DEBUG: Cause type: " + e.getCause().getClass().getName());
            }
            
            throw e;
        }
    }

    public List<Map<String, Object>> getAssignedEmployees(Long supervisorId) {
        try {
            System.out.println("=== DEBUG: UserService.getAssignedEmployees called with supervisorId: " + supervisorId);
            
            // Get the supervisor profile
            UserAccount supervisorAccount = userAccountRepository.findById(supervisorId)
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));
            
            if (supervisorAccount.getRole() != UserRole.SUPERVISOR) {
                throw new RuntimeException("User is not a supervisor");
            }
            
            SupervisorProfile supervisorProfile = (SupervisorProfile) supervisorAccount.getProfile();
            if (supervisorProfile == null) {
                throw new RuntimeException("Supervisor profile not found");
            }
            
            // Get all employees assigned to this supervisor
            List<EmployeeProfile> assignedEmployees = employeeProfileRepository.findBySupervisorProfileId(supervisorProfile.getId());
            System.out.println("=== DEBUG: Found " + assignedEmployees.size() + " assigned employees");
            
            // Convert to DTOs
            List<Map<String, Object>> employeeDtos = new ArrayList<>();
            for (EmployeeProfile employee : assignedEmployees) {
                try {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", employee.getUserAccount().getId());
                    dto.put("username", employee.getUserAccount().getUsername());
                    dto.put("email", employee.getUserAccount().getEmail());
                    dto.put("firstName", employee.getFirstName());
                    dto.put("lastName", employee.getLastName());
                    dto.put("department", employee.getDepartment());
                    dto.put("position", employee.getPosition());
                    dto.put("enabled", employee.getUserAccount().isEnabled());
                    employeeDtos.add(dto);
                } catch (Exception userEx) {
                    System.err.println("=== DEBUG: Error processing employee " + employee.getId() + ": " + userEx.getMessage());
                }
            }
            
            return employeeDtos;
        } catch (Exception e) {
            System.err.println("=== DEBUG: Exception in UserService.getAssignedEmployees: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
