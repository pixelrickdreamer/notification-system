package com.example.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private final List<Consumer<Notification>> listeners = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void consume(Notification notification) {
        log.info("Received notification from Kafka: {}", notification.id());
        notifications.add(notification);

        // Notify all SSE listeners
        listeners.forEach(listener -> {
            try {
                listener.accept(notification);
            } catch (Exception e) {
                log.warn("Failed to notify listener", e);
            }
        });
    }

    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public void addListener(Consumer<Notification> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Notification> listener) {
        listeners.remove(listener);
    }
}
