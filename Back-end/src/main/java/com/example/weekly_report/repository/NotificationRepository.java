package com.example.weekly_report.repository;

import com.example.weekly_report.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") Long recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.parent.id = :parentId ORDER BY n.createdAt ASC")
    List<Notification> findByParentIdOrderByCreatedAtAsc(@Param("parentId") Long parentId);

    @Modifying
    @Query("UPDATE Notification n SET n.report = NULL WHERE n.report.id = :reportId")
    void detachReportReferences(@Param("reportId") Long reportId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.report.id = :reportId")
    void deleteByReportId(@Param("reportId") Long reportId);
}


