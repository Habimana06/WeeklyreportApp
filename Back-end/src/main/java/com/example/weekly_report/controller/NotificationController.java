package com.example.weekly_report.controller;

import com.example.weekly_report.entity.Notification;
import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> listMine(Authentication authentication) {
        try {
            UserAccount me = (UserAccount) authentication.getPrincipal();
            List<Notification> list = notificationService.listForUser(me.getId());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/threads/{id}")
    public ResponseEntity<?> listThread(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(notificationService.listThread(id));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> send(@RequestParam Long recipientId,
                                  @RequestParam(required = false) Long reportId,
                                  @RequestParam String subject,
                                  @RequestParam String body,
                                  @RequestParam(required = false) Long parentId,
                                  Authentication authentication) {
        try {
            UserAccount me = (UserAccount) authentication.getPrincipal();
            Notification n = notificationService.send(me.getId(), recipientId, reportId, subject, body, parentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(n);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean read) {
        try {
            notificationService.markRead(id, read);
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "updated");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}


