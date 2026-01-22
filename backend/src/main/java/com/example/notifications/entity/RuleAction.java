package com.example.notifications.entity;

public enum RuleAction {
    FLAG,       // Mark as suspicious, continue processing
    BLOCK,      // Stop processing, don't pass on
    ROUTE,      // Send to specific Kafka topic
    ENRICH      // Add metadata/score and continue
}
