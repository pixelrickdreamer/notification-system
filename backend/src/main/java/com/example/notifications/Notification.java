package com.example.notifications;

import java.time.Instant;
import java.util.UUID;

public record Notification(
    String id,
    String userId,
    String type,
    String message,
    Instant timestamp
) {
    public static Notification create(String userId, String type, String message) {
        return new Notification(
            UUID.randomUUID().toString(),
            userId,
            type,
            message,
            Instant.now()
        );
    }
}
