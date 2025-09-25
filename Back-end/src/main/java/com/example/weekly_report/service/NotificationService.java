package com.example.weekly_report.service;

import com.example.weekly_report.entity.Notification;
import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.entity.WeeklyReport;
import com.example.weekly_report.repository.NotificationRepository;
import com.example.weekly_report.repository.UserAccountRepository;
import com.example.weekly_report.repository.WeeklyReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    public List<Notification> listForUser(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    public List<Notification> listThread(Long threadRootId) {
        return notificationRepository.findByParentIdOrderByCreatedAtAsc(threadRootId);
    }

    public Notification send(Long senderId, Long recipientId, Long reportId, String subject, String body, Long parentId) {
        UserAccount sender = userAccountRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        UserAccount recipient = userAccountRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        WeeklyReport report = null;
        if (reportId != null) {
            report = weeklyReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
        }

        Notification parent = null;
        if (parentId != null) {
            parent = notificationRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent notification not found"));
        }

        Notification n = new Notification();
        n.setSender(sender);
        n.setRecipient(recipient);
        n.setReport(report);
        n.setParent(parent);
        n.setSubject(subject);
        n.setBody(body);
        return notificationRepository.save(n);
    }

    public void markRead(Long notificationId, boolean read) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(read);
        notificationRepository.save(n);
    }
}


