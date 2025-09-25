package com.example.weekly_report.repository;

import com.example.weekly_report.entity.ReportAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportAttachmentRepository extends JpaRepository<ReportAttachment, Long> {
    List<ReportAttachment> findByReportIdOrderByCreatedAtDesc(Long reportId);
}


