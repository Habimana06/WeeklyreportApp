package com.example.weekly_report.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CreateReportRequest {
    
    @NotNull(message = "Week start date is required")
    private LocalDate weekStartDate;
    
    @NotNull(message = "Week end date is required")
    private LocalDate weekEndDate;
    
    @NotBlank(message = "Accomplished tasks are required")
    @Size(min = 10, message = "Accomplished tasks must be at least 10 characters")
    private String accomplishedTasks;
    
    private String challengesFaced;
    
    private String nextWeekPlans;
    
    private String additionalComments;
    
    @Min(value = 0, message = "Hours worked cannot be negative")
    @Max(value = 168, message = "Hours worked cannot exceed 168 per week")
    private Double hoursWorked;
    
    // Constructors
    public CreateReportRequest() {}
    
    public CreateReportRequest(LocalDate weekStartDate, LocalDate weekEndDate, String accomplishedTasks) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.accomplishedTasks = accomplishedTasks;
    }
    
    // Getters and Setters
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }
    
    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }
    
    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }
    
    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }
    
    public String getAccomplishedTasks() {
        return accomplishedTasks;
    }
    
    public void setAccomplishedTasks(String accomplishedTasks) {
        this.accomplishedTasks = accomplishedTasks;
    }
    
    public String getChallengesFaced() {
        return challengesFaced;
    }
    
    public void setChallengesFaced(String challengesFaced) {
        this.challengesFaced = challengesFaced;
    }
    
    public String getNextWeekPlans() {
        return nextWeekPlans;
    }
    
    public void setNextWeekPlans(String nextWeekPlans) {
        this.nextWeekPlans = nextWeekPlans;
    }
    
    public String getAdditionalComments() {
        return additionalComments;
    }
    
    public void setAdditionalComments(String additionalComments) {
        this.additionalComments = additionalComments;
    }
    
    public Double getHoursWorked() {
        return hoursWorked;
    }
    
    public void setHoursWorked(Double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
}