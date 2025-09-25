package com.example.weekly_report.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "week_start_date"})
})
public class WeeklyReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "week_start_date", nullable = false)
    @NotNull(message = "Week start date is required")
    private LocalDate weekStartDate;
    
    @Column(name = "week_end_date", nullable = false)
    @NotNull(message = "Week end date is required")
    private LocalDate weekEndDate;
    
    @Column(name = "accomplished_tasks", columnDefinition = "TEXT")
    @NotBlank(message = "Accomplished tasks are required")
    private String accomplishedTasks;
    
    @Column(name = "challenges_faced", columnDefinition = "TEXT")
    private String challengesFaced;
    
    @Column(name = "next_week_plans", columnDefinition = "TEXT")
    private String nextWeekPlans;
    
    @Column(name = "additional_comments", columnDefinition = "TEXT")
    private String additionalComments;
    
    @Column(name = "hours_worked")
    @Min(value = 0, message = "Hours worked cannot be negative")
    @Max(value = 168, message = "Hours worked cannot exceed 168 per week")
    private Double hoursWorked;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "supervisor_feedback", columnDefinition = "TEXT")
    private String supervisorFeedback;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference
    private EmployeeProfile employee;
    
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReportAttachment> attachments = new ArrayList<>();
    
    public List<ReportAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<ReportAttachment> attachments) { this.attachments = attachments; }
    
    // Constructors
    public WeeklyReport() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public WeeklyReport(LocalDate weekStartDate, LocalDate weekEndDate, String accomplishedTasks) {
        this();
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.accomplishedTasks = accomplishedTasks;
    }
    
    // Business methods
    public void submit() {
        this.status = ReportStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
    
    public void approve() {
        this.status = ReportStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public void reject() {
        this.status = ReportStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public boolean canBeEdited() {
        return status == ReportStatus.DRAFT || status == ReportStatus.REJECTED;
    }
    
    public boolean isSubmitted() {
        return status != ReportStatus.DRAFT;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReportStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
    
    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getSupervisorFeedback() {
        return supervisorFeedback;
    }
    
    public void setSupervisorFeedback(String supervisorFeedback) {
        this.supervisorFeedback = supervisorFeedback;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public EmployeeProfile getEmployee() {
        return employee;
    }
    
    public void setEmployee(EmployeeProfile employee) {
        this.employee = employee;
    }
    
    // Alias getters for frontend compatibility
    public String getAccomplishments() { return accomplishedTasks; }
    public String getChallenges() { return challengesFaced; }
    public String getNextWeekGoals() { return nextWeekPlans; }
    public String getAdditionalNotes() { return additionalComments; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

