package com.example.weekly_report.config;

import com.example.weekly_report.entity.*;
import com.example.weekly_report.repository.UserAccountRepository;
import com.example.weekly_report.repository.AdminProfileRepository;
import com.example.weekly_report.repository.EmployeeProfileRepository;
import com.example.weekly_report.repository.SupervisorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {
    
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
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize data if no users exist
        if (userAccountRepository.count() == 0) {
            initializeSampleData();
        }
    }
    
    private void initializeSampleData() {
        // Create Admin User
        UserAccount adminUser = new UserAccount();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@company.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setEnabled(true);
        adminUser = userAccountRepository.save(adminUser);
        
        AdminProfile adminProfile = new AdminProfile();
        adminProfile.setFirstName("System");
        adminProfile.setLastName("Administrator");
        adminProfile.setDepartment("IT");
        adminProfile.setPosition("System Admin");
        adminProfile.setPhoneNumber("+1234567890");
        adminProfile.setAdminId("ADM001");
        adminProfile.setUserAccount(adminUser);
        adminProfileRepository.save(adminProfile);
        
        // Create Supervisor User
        UserAccount supervisorUser = new UserAccount();
        supervisorUser.setUsername("supervisor");
        supervisorUser.setEmail("supervisor@company.com");
        supervisorUser.setPassword(passwordEncoder.encode("super123"));
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setEnabled(true);
        supervisorUser = userAccountRepository.save(supervisorUser);
        
        SupervisorProfile supervisorProfile = new SupervisorProfile();
        supervisorProfile.setFirstName("John");
        supervisorProfile.setLastName("Manager");
        supervisorProfile.setDepartment("Engineering");
        supervisorProfile.setPosition("Team Lead");
        supervisorProfile.setPhoneNumber("+1234567891");
        supervisorProfile.setSupervisorId("SUP001");
        supervisorProfile.setMaxEmployees(5);
        supervisorProfile.setUserAccount(supervisorUser);
        supervisorProfileRepository.save(supervisorProfile);
        
        // Create Employee Users
        for (int i = 1; i <= 3; i++) {
            UserAccount employeeUser = new UserAccount();
            employeeUser.setUsername("employee" + i);
            employeeUser.setEmail("employee" + i + "@company.com");
            employeeUser.setPassword(passwordEncoder.encode("emp123"));
            employeeUser.setRole(UserRole.EMPLOYEE);
            employeeUser.setEnabled(true);
            employeeUser = userAccountRepository.save(employeeUser);
            
            EmployeeProfile employeeProfile = new EmployeeProfile();
            employeeProfile.setFirstName("Employee" + i);
            employeeProfile.setLastName("User");
            employeeProfile.setDepartment("Engineering");
            employeeProfile.setPosition("Software Developer");
            employeeProfile.setPhoneNumber("+123456789" + (i + 1));
            employeeProfile.setEmployeeId("EMP00" + i);
            employeeProfile.setSupervisor(supervisorProfile);
            employeeProfile.setUserAccount(employeeUser);
            employeeProfileRepository.save(employeeProfile);
        }
        
        System.out.println("Sample data initialized successfully!");
        System.out.println("Admin credentials: admin / admin123");
        System.out.println("Supervisor credentials: supervisor / super123");
        System.out.println("Employee credentials: employee1 / emp123, employee2 / emp123, employee3 / emp123");
    }
}
