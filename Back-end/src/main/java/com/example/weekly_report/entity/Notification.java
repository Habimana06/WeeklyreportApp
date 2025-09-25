package com.example.weekly_report.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private UserAccount sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnore
    private UserAccount recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    @JsonIgnore
    private WeeklyReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Notification parent;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Denormalized helper fields for quick display
    @Column(name = "sender_username")
    private String senderUsername;

    @Column(name = "recipient_username")
    private String recipientUsername;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAccount getSender() { return sender; }
    public void setSender(UserAccount sender) { this.sender = sender; this.senderUsername = sender != null ? sender.getUsername() : null; }
    public UserAccount getRecipient() { return recipient; }
    public void setRecipient(UserAccount recipient) { this.recipient = recipient; this.recipientUsername = recipient != null ? recipient.getUsername() : null; }
    public WeeklyReport getReport() { return report; }
    public void setReport(WeeklyReport report) { this.report = report; }
    public Notification getParent() { return parent; }
    public void setParent(Notification parent) { this.parent = parent; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getSenderUsername() { return senderUsername; }
    public String getRecipientUsername() { return recipientUsername; }
    
    // Additional getters for frontend convenience
    public Long getSenderId() { return sender != null ? sender.getId() : null; }
    public Long getRecipientId() { return recipient != null ? recipient.getId() : null; }
    public Long getReportId() { return report != null ? report.getId() : null; }
}


