package com.example.weekly_report.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "admin_profiles")
@PrimaryKeyJoinColumn(name = "profile_id")
public class AdminProfile extends BaseProfile {
    
    @Column(name = "admin_id", unique = true)
    private String adminId;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "salary")
    private Double salary;
    
    @Column(name = "access_level")
    @Enumerated(EnumType.STRING)
    private AdminAccessLevel accessLevel = AdminAccessLevel.FULL;
    
    // Constructors
    public AdminProfile() {
        super();
        this.hireDate = LocalDate.now();
    }
    
    public AdminProfile(String firstName, String lastName, String department, String position, String adminId) {
        super(firstName, lastName, department, position);
        this.adminId = adminId;
        this.hireDate = LocalDate.now();
    }
    
    // Business methods
    public boolean hasFullAccess() {
        return accessLevel == AdminAccessLevel.FULL;
    }
    
    public boolean canManageUsers() {
        return accessLevel == AdminAccessLevel.FULL || accessLevel == AdminAccessLevel.USER_MANAGEMENT;
    }
    
    public boolean canViewReports() {
        return accessLevel != AdminAccessLevel.READ_ONLY;
    }
    
    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }
    
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    
    public Double getSalary() {
        return salary;
    }
    
    public void setSalary(Double salary) {
        this.salary = salary;
    }
    
    public AdminAccessLevel getAccessLevel() {
        return accessLevel;
    }
    
    public void setAccessLevel(AdminAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}

enum AdminAccessLevel {
    READ_ONLY,
    USER_MANAGEMENT,
    FULL
}