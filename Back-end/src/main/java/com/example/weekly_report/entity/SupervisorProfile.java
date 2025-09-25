package com.example.weekly_report.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supervisor_profiles")
@PrimaryKeyJoinColumn(name = "profile_id")
public class SupervisorProfile extends BaseProfile {
    
    @Column(name = "supervisor_id", unique = true)
    private String supervisorId;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "salary")
    private Double salary;
    
    @Column(name = "max_employees")
    private Integer maxEmployees = 10;
    
    @OneToMany(mappedBy = "supervisor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EmployeeProfile> supervisedEmployees = new ArrayList<>();
    
    // Constructors
    public SupervisorProfile() {
        super();
        this.hireDate = LocalDate.now();
    }
    
    public SupervisorProfile(String firstName, String lastName, String department, String position, String supervisorId) {
        super(firstName, lastName, department, position);
        this.supervisorId = supervisorId;
        this.hireDate = LocalDate.now();
    }
    
    // Business methods
    public void addEmployee(EmployeeProfile employee) {
        if (supervisedEmployees.size() < maxEmployees) {
            supervisedEmployees.add(employee);
            employee.setSupervisor(this);
        } else {
            throw new IllegalStateException("Maximum number of employees reached");
        }
    }
    
    public void removeEmployee(EmployeeProfile employee) {
        supervisedEmployees.remove(employee);
        employee.setSupervisor(null);
    }
    
    public boolean canSuperviseMore() {
        return supervisedEmployees.size() < maxEmployees;
    }
    
    // Getters and Setters
    public String getSupervisorId() {
        return supervisorId;
    }
    
    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
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
    
    public Integer getMaxEmployees() {
        return maxEmployees;
    }
    
    public void setMaxEmployees(Integer maxEmployees) {
        this.maxEmployees = maxEmployees;
    }
    
    public List<EmployeeProfile> getSupervisedEmployees() {
        return supervisedEmployees;
    }
    
    public void setSupervisedEmployees(List<EmployeeProfile> supervisedEmployees) {
        this.supervisedEmployees = supervisedEmployees;
    }
}
