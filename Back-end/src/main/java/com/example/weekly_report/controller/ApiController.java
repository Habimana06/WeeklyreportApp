package com.example.weekly_report.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class ApiController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "OK");
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Weekly Report API");
        body.put("version", "v1");
        body.put("endpoints", List.of(
                "/api/v1/health",
                "/api/v1/",
                "/api/v1/reports",
                "/api/v1/reports/{id}"
        ));
        return ResponseEntity.ok(body);
    }

    // Sample in-memory reports for demonstration (non-persistent)
    private final Map<Long, Map<String, Object>> demoReports = Collections.synchronizedMap(new LinkedHashMap<>());
    private long idSequence = 1L;

    @GetMapping("/reports")
    public ResponseEntity<List<Map<String, Object>>> getAllReports() {
        return ResponseEntity.ok(new ArrayList<>(demoReports.values()));
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        Map<String, Object> report = demoReports.get(id);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Report not found"));
        }
        return ResponseEntity.ok(report);
    }

    @PostMapping("/reports")
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody Map<String, Object> payload) {
        long id = idSequence++;
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("id", id);
        report.put("createdAt", LocalDateTime.now().toString());
        report.put("status", Optional.ofNullable(payload.get("status")).orElse("DRAFT"));
        report.put("week", payload.getOrDefault("week", "unknown"));
        report.put("title", payload.getOrDefault("title", "Untitled Report"));
        demoReports.put(id, report);
        return ResponseEntity.status(201).body(report);
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<?> updateReport(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Map<String, Object> report = demoReports.get(id);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Report not found"));
        }
        if (payload.containsKey("status")) report.put("status", payload.get("status"));
        if (payload.containsKey("week")) report.put("week", payload.get("week"));
        if (payload.containsKey("title")) report.put("title", payload.get("title"));
        report.put("updatedAt", LocalDateTime.now().toString());
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        Map<String, Object> removed = demoReports.remove(id);
        if (removed == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Report not found"));
        }
        return ResponseEntity.ok(Map.of("message", "Report deleted"));
    }
}


