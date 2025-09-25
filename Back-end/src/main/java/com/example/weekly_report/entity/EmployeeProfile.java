package com.example.weekly_report.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_profiles")
@PrimaryKeyJoinColumn(name = "profile_id")
public class EmployeeProfile extends BaseProfile {
    
    @Column(name = "employee_id", unique = true)
    private String employeeId;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "salary")
    private Double salary;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    @JsonBackReference
    private SupervisorProfile supervisor;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<WeeklyReport> weeklyReports = new ArrayList<>();
    
    // Constructors
    public EmployeeProfile() {
        super();
        this.hireDate = LocalDate.now();
    }
    
    public EmployeeProfile(String firstName, String lastName, String department, String position, String employeeId) {
        super(firstName, lastName, department, position);
        this.employeeId = employeeId;
        this.hireDate = LocalDate.now();
    }
    
    // Business methods
    public void addWeeklyReport(WeeklyReport report) {
        weeklyReports.add(report);
        report.setEmployee(this);
    }
    
    public void removeWeeklyReport(WeeklyReport report) {
        weeklyReports.remove(report);
        report.setEmployee(null);
    }
    
    public boolean hasSupervisor() {
        return supervisor != null;
    }
    
    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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
    
    public SupervisorProfile getSupervisor() {
        return supervisor;
    }
    
    public void setSupervisor(SupervisorProfile supervisor) {
        this.supervisor = supervisor;
    }
    
    public List<WeeklyReport> getWeeklyReports() {
        return weeklyReports;
    }
    
    public void setWeeklyReports(List<WeeklyReport> weeklyReports) {
        this.weeklyReports = weeklyReports;
    }
}