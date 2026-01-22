package com.example.notifications.repository;

import com.example.notifications.entity.AuditLog;
import com.example.notifications.entity.RuleAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByProcessedAtDesc(Pageable pageable);

    long countByFinalAction(RuleAction action);

    long countByProcessedAtAfter(Instant after);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.rulesMatched > 0")
    long countFlagged();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.rulesMatched = 0")
    long countClean();
}
