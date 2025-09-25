package com.example.weekly_report.dto;

import jakarta.validation.constraints.NotNull;

public class AssignSupervisorRequest {
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotNull(message = "Supervisor ID is required")
    private Long supervisorId;
    
    // Constructors
    public AssignSupervisorRequest() {}
    
    public AssignSupervisorRequest(Long employeeId, Long supervisorId) {
        this.employeeId = employeeId;
        this.supervisorId = supervisorId;
    }
    
    // Getters and Setters
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
    
    public Long getSupervisorId() {
        return supervisorId;
    }
    
    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }
}