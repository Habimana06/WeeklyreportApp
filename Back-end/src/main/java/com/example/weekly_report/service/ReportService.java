package com.example.weekly_report.service;

import com.example.weekly_report.dto.CreateReportRequest;
import com.example.weekly_report.entity.*;
import com.example.weekly_report.repository.WeeklyReportRepository;
import com.example.weekly_report.repository.EmployeeProfileRepository;
import com.example.weekly_report.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class ReportService {
    
    @Autowired
    private WeeklyReportRepository weeklyReportRepository;
    
    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;
    
    public WeeklyReport createReport(CreateReportRequest request, Long employeeId) {
        EmployeeProfile employee = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Check if report already exists for this week
        LocalDate weekStart = request.getWeekStartDate();
        Optional<WeeklyReport> existingReport = weeklyReportRepository
                .findByEmployeeIdAndWeekStartDate(employeeId, weekStart);
        
        if (existingReport.isPresent()) {
            throw new RuntimeException("Report already exists for this week");
        }
        
        WeeklyReport report = new WeeklyReport();
        report.setWeekStartDate(request.getWeekStartDate());
        report.setWeekEndDate(request.getWeekEndDate());
        report.setAccomplishedTasks(request.getAccomplishedTasks());
        report.setChallengesFaced(request.getChallengesFaced());
        report.setNextWeekPlans(request.getNextWeekPlans());
        report.setAdditionalComments(request.getAdditionalComments());
        report.setHoursWorked(request.getHoursWorked());
        report.setStatus(ReportStatus.DRAFT);
        report.setEmployee(employee);
        
        return weeklyReportRepository.save(report);
    }
    
    public WeeklyReport updateReport(Long reportId, CreateReportRequest request, Long employeeId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        // Check if employee owns this report
        if (!report.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("You can only update your own reports");
        }
        
        // Check if report can be edited
        if (!report.canBeEdited()) {
            throw new RuntimeException("Report cannot be edited in current status");
        }
        
        report.setWeekStartDate(request.getWeekStartDate());
        report.setWeekEndDate(request.getWeekEndDate());
        report.setAccomplishedTasks(request.getAccomplishedTasks());
        report.setChallengesFaced(request.getChallengesFaced());
        report.setNextWeekPlans(request.getNextWeekPlans());
        report.setAdditionalComments(request.getAdditionalComments());
        report.setHoursWorked(request.getHoursWorked());
        
        return weeklyReportRepository.save(report);
    }
    
    public WeeklyReport submitReport(Long reportId, Long employeeId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        // Check if employee owns this report
        if (!report.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("You can only submit your own reports");
        }
        
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new RuntimeException("Only draft reports can be submitted");
        }
        
        report.submit();
        report = weeklyReportRepository.save(report);

        // Send notification to supervisor
        if (report.getEmployee().getSupervisor() != null) {
            try {
                notificationService.send(
                    employeeId, // sender (employee)
                    report.getEmployee().getSupervisor().getUserAccount().getId(), // recipient (supervisor)
                    reportId, // report ID
                    "Weekly Report Submitted",
                    String.format("Weekly report for week of %s has been submitted by %s",
                        report.getWeekStartDate(), report.getEmployee().getUserAccount().getUsername()),
                    null // no parent notification
                );
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        }
        
        return report;
    }
    
    public WeeklyReport approveReport(Long reportId, String feedback) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new RuntimeException("Only submitted reports can be approved");
        }
        
        report.approve();
        if (feedback != null && !feedback.trim().isEmpty()) {
            report.setSupervisorFeedback(feedback);
        }
        
        report = weeklyReportRepository.save(report);

        // Send notification to employee
        try {
            String message = feedback != null && !feedback.trim().isEmpty() 
                ? String.format("Your weekly report for week of %s has been approved with feedback: %s",
                    report.getWeekStartDate(), feedback)
                : String.format("Your weekly report for week of %s has been approved",
                    report.getWeekStartDate());

            notificationService.send(
                report.getEmployee().getSupervisor() != null ? 
                    report.getEmployee().getSupervisor().getUserAccount().getId() : 1L, // sender (supervisor)
                report.getEmployee().getUserAccount().getId(), // recipient (employee)
                reportId, // report ID
                "Report Approved",
                message,
                null // no parent notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return report;
    }
    
    public WeeklyReport rejectReport(Long reportId, String feedback) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new RuntimeException("Only submitted reports can be rejected");
        }
        
        report.reject();
        if (feedback != null && !feedback.trim().isEmpty()) {
            report.setSupervisorFeedback(feedback);
        }
        
        report = weeklyReportRepository.save(report);

        // Send notification to employee
        try {
            String message = feedback != null && !feedback.trim().isEmpty() 
                ? String.format("Your weekly report for week of %s has been rejected with feedback: %s",
                    report.getWeekStartDate(), feedback)
                : String.format("Your weekly report for week of %s has been rejected",
                    report.getWeekStartDate());

            notificationService.send(
                report.getEmployee().getSupervisor() != null ? 
                    report.getEmployee().getSupervisor().getUserAccount().getId() : 1L, // sender (supervisor)
                report.getEmployee().getUserAccount().getId(), // recipient (employee)
                reportId, // report ID
                "Report Rejected",
                message,
                null // no parent notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return report;
    }
    
    public List<WeeklyReport> getReportsByEmployee(Long employeeId) {
        return weeklyReportRepository.findByEmployeeIdOrderByWeekStartDateDesc(employeeId);
    }
    
    public List<WeeklyReport> getReportsBySupervisor(Long supervisorId) {
        return weeklyReportRepository.findByEmployeeSupervisorIdOrderByWeekStartDateDesc(supervisorId);
    }
    
    public List<WeeklyReport> getAllReports() {
        return weeklyReportRepository.findAllByOrderByWeekStartDateDesc();
    }
    
    public List<WeeklyReport> getReportsByStatus(ReportStatus status) {
        return weeklyReportRepository.findByStatusOrderByWeekStartDateDesc(status);
    }
    
    public Optional<WeeklyReport> getReportById(Long id) {
        return weeklyReportRepository.findById(id);
    }
    
    public void deleteReport(Long reportId, Long employeeId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        // Check if employee owns this report
        if (!report.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("You can only delete your own reports");
        }
        
        // Only allow deletion of draft reports
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new RuntimeException("Only draft reports can be deleted");
        }
        
        weeklyReportRepository.delete(report);
    }

    public void supervisorDeleteDraft(Long reportId, Long supervisorProfileId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        if (report.getEmployee().getSupervisor() == null ||
            !report.getEmployee().getSupervisor().getId().equals(supervisorProfileId)) {
            throw new RuntimeException("Report not under your supervision");
        }
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new RuntimeException("Only draft reports can be deleted");
        }
        weeklyReportRepository.delete(report);
    }

    public void adminDeleteReport(Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        // Detach or delete notifications referencing this report to satisfy FK
        try {
            notificationRepository.detachReportReferences(reportId);
        } catch (Exception ignored) {}
        weeklyReportRepository.delete(report);
    }
    
    public List<WeeklyReport> getReportsForCurrentWeek() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate weekStart = now.with(weekFields.dayOfWeek(), 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        return weeklyReportRepository.findByWeekStartDateBetween(weekStart, weekEnd);
    }
    
    public List<WeeklyReport> getReportsForDateRange(LocalDate startDate, LocalDate endDate) {
        return weeklyReportRepository.findByWeekStartDateBetween(startDate, endDate);
    }

    public WeeklyReport overrideReportStatus(Long reportId, ReportStatus newStatus, String feedback) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        // Store old status for notification
        ReportStatus oldStatus = report.getStatus();
        
        // Update status
        report.setStatus(newStatus);
        
        // Set appropriate timestamps
        if (newStatus == ReportStatus.SUBMITTED && oldStatus == ReportStatus.DRAFT) {
            report.setSubmittedAt(LocalDateTime.now());
        } else if (newStatus == ReportStatus.APPROVED && oldStatus == ReportStatus.SUBMITTED) {
            report.setApprovedAt(LocalDateTime.now());
        } else if (newStatus == ReportStatus.REJECTED && oldStatus == ReportStatus.SUBMITTED) {
            report.setRejectedAt(LocalDateTime.now());
        }
        
        // Set feedback if provided
        if (feedback != null && !feedback.trim().isEmpty()) {
            report.setSupervisorFeedback(feedback);
        }
        
        report = weeklyReportRepository.save(report);
        
        // Send notification about status change
        try {
            String statusChangeMessage = String.format("Your weekly report for week of %s status has been changed from %s to %s%s", 
                report.getWeekStartDate(), 
                oldStatus, 
                newStatus,
                feedback != null && !feedback.trim().isEmpty() ? " with feedback: " + feedback : "");
            
            notificationService.send(
                report.getEmployee().getSupervisor() != null ? 
                    report.getEmployee().getSupervisor().getUserAccount().getId() : 1L, // sender (admin or system)
                report.getEmployee().getUserAccount().getId(), // recipient (employee)
                reportId, // report ID
                "Report Status Changed",
                statusChangeMessage,
                null // no parent notification
            );
        } catch (Exception e) {
            // Log error but don't fail the status override
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return report;
    }
}
