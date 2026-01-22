package com.example.notifications.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Application(
    String id,
    String type,
    String sourceSystem,
    Map<String, Object> data,
    Instant receivedAt
) {
    public static Application fromKafkaMessage(String topic, Map<String, Object> payload) {
        String id = (String) payload.getOrDefault("id", UUID.randomUUID().toString());
        String type = (String) payload.getOrDefault("type", "unknown");
        String source = (String) payload.getOrDefault("source", "unknown");

        return new Application(id, type, source, payload, Instant.now());
    }

    @SuppressWarnings("unchecked")
    public Object getFieldValue(String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
