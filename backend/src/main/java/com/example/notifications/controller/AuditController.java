package com.example.notifications.controller;

import com.example.notifications.entity.AuditLog;
import com.example.notifications.entity.RuleAction;
import com.example.notifications.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "http://localhost:5173")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public Page<AuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditLogRepository.findAllByOrderByProcessedAtDesc(
            PageRequest.of(page, size)
        );
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);

        long total = auditLogRepository.count();
        long last24h = auditLogRepository.countByProcessedAtAfter(last24Hours);
        long flagged = auditLogRepository.countFlagged();
        long clean = auditLogRepository.countClean();
        long blocked = auditLogRepository.countByFinalAction(RuleAction.BLOCK);

        return Map.of(
            "total", total,
            "last24Hours", last24h,
            "flagged", flagged,
            "clean", clean,
            "blocked", blocked,
            "flagRate", total > 0 ? (double) flagged / total * 100 : 0
        );
    }
}
