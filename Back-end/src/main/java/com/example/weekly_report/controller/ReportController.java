package com.example.weekly_report.controller;

import com.example.weekly_report.dto.CreateReportRequest;
import com.example.weekly_report.entity.ReportStatus;
import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.entity.WeeklyReport;
import com.example.weekly_report.service.ReportService;
import com.example.weekly_report.dto.WeeklyReportView;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private ReportService reportService;

    @Autowired
    private com.example.weekly_report.service.PdfService pdfService;
    
    @PostMapping
    public ResponseEntity<?> createReport(@Valid @RequestBody CreateReportRequest request, 
                                        Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("EMPLOYEE")) {
                WeeklyReport report = reportService.createReport(request, user.getProfile().getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(report);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only employees can create reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId,
                                        @Valid @RequestBody CreateReportRequest request,
                                        Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("EMPLOYEE")) {
                WeeklyReport report = reportService.updateReport(reportId, request, user.getProfile().getId());
                return ResponseEntity.ok(report);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only employees can update reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{reportId}/submit")
    public ResponseEntity<?> submitReport(@PathVariable Long reportId, Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("EMPLOYEE")) {
                WeeklyReport report = reportService.submitReport(reportId, user.getProfile().getId());
                return ResponseEntity.ok(report);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only employees can submit reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<?> approveReport(@PathVariable Long reportId,
                                         @RequestParam(required = false) String feedback,
                                         Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("SUPERVISOR") || user.getRole().name().equals("ADMIN")) {
                WeeklyReport report = reportService.approveReport(reportId, feedback);
                return ResponseEntity.ok(report);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only supervisors and admins can approve reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<?> rejectReport(@PathVariable Long reportId,
                                        @RequestParam(required = false) String feedback,
                                        Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("SUPERVISOR") || user.getRole().name().equals("ADMIN")) {
                WeeklyReport report = reportService.rejectReport(reportId, feedback);
                return ResponseEntity.ok(report);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only supervisors and admins can reject reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping(value = "/{reportId}/pdf")
    public ResponseEntity<?> downloadReportPdf(@PathVariable Long reportId, Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            Optional<WeeklyReport> reportOpt = reportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Report not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            WeeklyReport report = reportOpt.get();
            boolean canView = false;
            switch (user.getRole().name()) {
                case "ADMIN":
                    canView = true; break;
                case "SUPERVISOR":
                    canView = report.getEmployee().getSupervisor() != null &&
                              report.getEmployee().getSupervisor().getId().equals(user.getProfile().getId());
                    break;
                case "EMPLOYEE":
                    canView = report.getEmployee().getId().equals(user.getProfile().getId());
                    break;
            }
            if (!canView) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You don't have permission to download this report");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            byte[] pdf = pdfService.generateReportPdf(report);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + reportId + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{reportId}/override-status")
    public ResponseEntity<?> overrideReportStatus(@PathVariable Long reportId,
                                                @RequestParam ReportStatus status,
                                                @RequestParam(required = false) String feedback,
                                                Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("ADMIN")) {
                WeeklyReport report = reportService.overrideReportStatus(reportId, status, feedback);
                return ResponseEntity.ok(report);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only admins can override report status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getReports(Authentication authentication,
                                      @RequestParam(required = false) ReportStatus status,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      @RequestParam(required = false) String date) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            List<WeeklyReport> reports;
            
            switch (user.getRole().name()) {
                case "ADMIN":
                    if (status != null) {
                        reports = reportService.getReportsByStatus(status);
                    } else if (startDate != null && endDate != null) {
                        reports = reportService.getReportsForDateRange(
                            LocalDate.parse(startDate), LocalDate.parse(endDate));
                    } else if (date != null) {
                        LocalDate d = LocalDate.parse(date);
                        reports = reportService.getReportsForDateRange(d, d);
                    } else {
                        reports = reportService.getAllReports();
                    }
                    break;
                    
                case "SUPERVISOR":
                    reports = reportService.getReportsBySupervisor(user.getProfile().getId());
                    break;
                    
                case "EMPLOYEE":
                    reports = reportService.getReportsByEmployee(user.getProfile().getId());
                    break;
                    
                default:
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid user role");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            // If single date filter provided, apply post-filter for non-admin scopes
            if (date != null) {
                LocalDate d = LocalDate.parse(date);
                reports = reports.stream()
                        .filter(r -> d.equals(r.getWeekStartDate()))
                        .collect(Collectors.toList());
            }
            List<WeeklyReportView> views = reports.stream().map(this::toView).collect(Collectors.toList());
            return ResponseEntity.ok(views);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId, Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            Optional<WeeklyReport> report = reportService.getReportById(reportId);
            
            if (report.isPresent()) {
                WeeklyReport reportEntity = report.get();
                
                // Check permissions
                boolean canView = false;
                switch (user.getRole().name()) {
                    case "ADMIN":
                        canView = true;
                        break;
                    case "SUPERVISOR":
                        canView = reportEntity.getEmployee().getSupervisor() != null &&
                                 reportEntity.getEmployee().getSupervisor().getId().equals(user.getProfile().getId());
                        break;
                    case "EMPLOYEE":
                        canView = reportEntity.getEmployee().getId().equals(user.getProfile().getId());
                        break;
                }
                
                if (canView) {
                    WeeklyReportView view = toView(reportEntity);
                    return ResponseEntity.ok(view);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "You don't have permission to view this report");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Report not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId, Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            String role = user.getRole().name();
            if (role.equals("ADMIN")) {
                // Admin can delete any report
                reportService.adminDeleteReport(reportId);
            } else if (role.equals("SUPERVISOR")) {
                // Supervisor can delete only DRAFT reports of their assigned employees
                reportService.supervisorDeleteDraft(reportId, user.getProfile().getId());
            } else if (role.equals("EMPLOYEE")) {
                // Employee can delete only own DRAFT reports
                reportService.deleteReport(reportId, user.getProfile().getId());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authorized to delete reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Report deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/current-week")
    public ResponseEntity<?> getCurrentWeekReports(Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("SUPERVISOR")) {
                List<WeeklyReport> reports = reportService.getReportsForCurrentWeek();
                List<WeeklyReportView> views = reports.stream().map(this::toView).collect(Collectors.toList());
                return ResponseEntity.ok(views);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only admins and supervisors can view current week reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    private WeeklyReportView toView(WeeklyReport reportEntity) {
        WeeklyReportView view = new WeeklyReportView();
        view.setId(reportEntity.getId());
        view.setWeekStartDate(reportEntity.getWeekStartDate());
        view.setWeekEndDate(reportEntity.getWeekEndDate());
        view.setStatus(reportEntity.getStatus());
        view.setCreatedAt(reportEntity.getCreatedAt());
        view.setSubmittedAt(reportEntity.getSubmittedAt());
        view.setApprovedAt(reportEntity.getApprovedAt());
        view.setRejectedAt(reportEntity.getRejectedAt());
        view.setSupervisorFeedback(reportEntity.getSupervisorFeedback());
        view.setAccomplishments(reportEntity.getAccomplishments());
        view.setChallenges(reportEntity.getChallenges());
        view.setNextWeekGoals(reportEntity.getNextWeekGoals());
        view.setAdditionalNotes(reportEntity.getAdditionalNotes());
        String empName = null;
        if (reportEntity.getEmployee() != null) {
            if (reportEntity.getEmployee().getUserAccount() != null) {
                empName = reportEntity.getEmployee().getUserAccount().getUsername();
                if (empName == null || empName.isBlank()) {
                    String email = reportEntity.getEmployee().getUserAccount().getEmail();
                    if (email != null && !email.isBlank()) {
                        empName = email.split("@")[0];
                    }
                }
            }
            if (empName == null || empName.isBlank()) {
                // fallback to full name from profile
                try { empName = reportEntity.getEmployee().getFullName(); } catch (Exception ignored) {}
            }
            if (empName == null || empName.isBlank()) {
                empName = "User " + reportEntity.getEmployee().getId();
            }
        }

        String supName = null;
        if (reportEntity.getEmployee() != null && reportEntity.getEmployee().getSupervisor() != null) {
            if (reportEntity.getEmployee().getSupervisor().getUserAccount() != null) {
                supName = reportEntity.getEmployee().getSupervisor().getUserAccount().getUsername();
                if (supName == null || supName.isBlank()) {
                    String email = reportEntity.getEmployee().getSupervisor().getUserAccount().getEmail();
                    if (email != null && !email.isBlank()) {
                        supName = email.split("@")[0];
                    }
                }
            }
            if (supName == null || supName.isBlank()) {
                try { supName = reportEntity.getEmployee().getSupervisor().getFullName(); } catch (Exception ignored) {}
            }
            if (supName == null || supName.isBlank()) {
                supName = "Supervisor " + reportEntity.getEmployee().getSupervisor().getId();
            }
        }
        view.setEmployeeUsername(empName);
        view.setSupervisorUsername(supName);
        // Also expose display names suitable for UI (prefer full name when available)
        String empDisplay = null;
        try { if (reportEntity.getEmployee() != null) empDisplay = reportEntity.getEmployee().getFullName(); } catch (Exception ignored) {}
        if (empDisplay == null || empDisplay.isBlank()) empDisplay = empName;
        String supDisplay = null;
        try { if (reportEntity.getEmployee() != null && reportEntity.getEmployee().getSupervisor() != null) supDisplay = reportEntity.getEmployee().getSupervisor().getFullName(); } catch (Exception ignored) {}
        if (supDisplay == null || supDisplay.isBlank()) supDisplay = supName;
        view.setEmployeeDisplayName(empDisplay);
        view.setSupervisorDisplayName(supDisplay);
        return view;
    }
}
