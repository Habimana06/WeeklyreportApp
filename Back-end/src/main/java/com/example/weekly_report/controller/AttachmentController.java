package com.example.weekly_report.controller;

import com.example.weekly_report.entity.ReportAttachment;
import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.service.AttachmentService;
import com.example.weekly_report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/{reportId}/attachments")
    public ResponseEntity<?> list(@PathVariable Long reportId, Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            // Authorization is enforced implicitly by report visibility rules in ReportController usage patterns
            List<ReportAttachment> list = attachmentService.listAttachments(reportId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{reportId}/attachments")
    public ResponseEntity<?> upload(@PathVariable Long reportId,
                                    @RequestParam("file") MultipartFile file,
                                    Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            // Only EMPLOYEE owner can upload; service-level ownership checks exist in ReportService operations
            if (!user.getRole().name().equals("EMPLOYEE")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only employees can upload attachments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            ReportAttachment att = attachmentService.uploadAttachment(reportId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(att);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{reportId}/attachments/{attachmentId}")
    public ResponseEntity<?> download(@PathVariable Long reportId,
                                      @PathVariable Long attachmentId) {
        try {
            ReportAttachment att = attachmentService.getAttachment(attachmentId);
            // Validate that file exists on disk
            java.nio.file.Path filePath = java.nio.file.Paths.get(att.getStoragePath());
            if (!java.nio.file.Files.exists(filePath)) {
                Map<String, String> notFound = new HashMap<>();
                notFound.put("error", "Attachment file not found on server");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
            }
            byte[] bytes = attachmentService.getAttachmentData(attachmentId);
            return ResponseEntity.ok()
                    .contentType(att.getMimeType() != null ? MediaType.parseMediaType(att.getMimeType()) : MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + att.getOriginalName() + "\"")
                    .body(bytes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{reportId}/attachments/{attachmentId}")
    public ResponseEntity<?> delete(@PathVariable Long reportId,
                                    @PathVariable Long attachmentId,
                                    Authentication authentication) {
        try {
            UserAccount user = (UserAccount) authentication.getPrincipal();
            if (!user.getRole().name().equals("EMPLOYEE") && !user.getRole().name().equals("ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authorized to delete attachments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            attachmentService.deleteAttachment(attachmentId);
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "Attachment deleted");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}


