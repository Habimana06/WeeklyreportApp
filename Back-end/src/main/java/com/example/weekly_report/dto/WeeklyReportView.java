package com.example.weekly_report.dto;

import com.example.weekly_report.entity.ReportStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeeklyReportView {
    private Long id;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private String supervisorFeedback;
    private String accomplishments;
    private String challenges;
    private String nextWeekGoals;
    private String additionalNotes;
    private String employeeUsername;
    private String supervisorUsername;
    private String employeeDisplayName;
    private String supervisorDisplayName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }
    public LocalDate getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(LocalDate weekEndDate) { this.weekEndDate = weekEndDate; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getSupervisorFeedback() { return supervisorFeedback; }
    public void setSupervisorFeedback(String supervisorFeedback) { this.supervisorFeedback = supervisorFeedback; }
    public String getAccomplishments() { return accomplishments; }
    public void setAccomplishments(String accomplishments) { this.accomplishments = accomplishments; }
    public String getChallenges() { return challenges; }
    public void setChallenges(String challenges) { this.challenges = challenges; }
    public String getNextWeekGoals() { return nextWeekGoals; }
    public void setNextWeekGoals(String nextWeekGoals) { this.nextWeekGoals = nextWeekGoals; }
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public String getEmployeeUsername() { return employeeUsername; }
    public void setEmployeeUsername(String employeeUsername) { this.employeeUsername = employeeUsername; }
    public String getSupervisorUsername() { return supervisorUsername; }
    public void setSupervisorUsername(String supervisorUsername) { this.supervisorUsername = supervisorUsername; }
    public String getEmployeeDisplayName() { return employeeDisplayName; }
    public void setEmployeeDisplayName(String employeeDisplayName) { this.employeeDisplayName = employeeDisplayName; }
    public String getSupervisorDisplayName() { return supervisorDisplayName; }
    public void setSupervisorDisplayName(String supervisorDisplayName) { this.supervisorDisplayName = supervisorDisplayName; }
}


