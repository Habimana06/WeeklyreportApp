package com.example.weekly_report.service;

import com.example.weekly_report.entity.ReportAttachment;
import com.example.weekly_report.entity.WeeklyReport;
import com.example.weekly_report.repository.ReportAttachmentRepository;
import com.example.weekly_report.repository.WeeklyReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Transactional
public class AttachmentService {
    
    @Autowired
    private WeeklyReportRepository weeklyReportRepository;
    
    @Autowired
    private ReportAttachmentRepository attachmentRepository;
    
    @Value("${app.storage.attachments:storage/attachments}")
    private String attachmentsRoot;
    
    public List<ReportAttachment> listAttachments(Long reportId) {
        return attachmentRepository.findByReportIdOrderByCreatedAtDesc(reportId);
    }
    
    public ReportAttachment uploadAttachment(Long reportId, MultipartFile file) throws IOException {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        Path dir = Paths.get(attachmentsRoot, String.valueOf(reportId));
        Files.createDirectories(dir);
        Path target = dir.resolve(System.currentTimeMillis() + "_" + file.getOriginalFilename());
        Files.copy(file.getInputStream(), target);
        
        ReportAttachment att = new ReportAttachment();
        att.setReport(report);
        att.setOriginalName(file.getOriginalFilename());
        att.setMimeType(file.getContentType());
        att.setSizeBytes(file.getSize());
        att.setStoragePath(target.toString());
        return attachmentRepository.save(att);
    }
    
    public byte[] getAttachmentData(Long attachmentId) throws IOException {
        ReportAttachment att = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return Files.readAllBytes(Paths.get(att.getStoragePath()));
    }
    
    public ReportAttachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }
    
    public void deleteAttachment(Long attachmentId) throws IOException {
        ReportAttachment att = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        Files.deleteIfExists(Paths.get(att.getStoragePath()));
        attachmentRepository.delete(att);
    }
}


