package com.example.notifications.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record IncomingEvent(
    String id,
    String source,
    String topic,
    String type,
    Map<String, Object> payload,
    Instant timestamp
) {
    public static IncomingEvent create(String source, String topic, String type, Map<String, Object> payload) {
        return new IncomingEvent(
            UUID.randomUUID().toString(),
            source,
            topic,
            type,
            payload,
            Instant.now()
        );
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayloadValue(String key) {
        return (T) payload.get(key);
    }

    public Number getPayloadNumber(String key) {
        Object value = payload.get(key);
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return null;
    }
}
