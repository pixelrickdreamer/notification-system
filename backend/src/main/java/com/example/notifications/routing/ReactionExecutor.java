package com.example.notifications.routing;

import com.example.notifications.Notification;
import com.example.notifications.model.Reaction;
import com.example.notifications.model.Reaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class ReactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReactionExecutor.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestClient restClient;
    private final List<Consumer<Notification>> frontendListeners = new CopyOnWriteArrayList<>();

    public ReactionExecutor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.restClient = RestClient.create();
    }

    public void execute(Reaction reaction) {
        switch (reaction) {
            case PublishToKafka r -> executeKafkaPublish(r);
            case PushToFrontend r -> executeFrontendPush(r);
            case CallExternalApi r -> executeApiCall(r);
            case LogEvent r -> executeLogEvent(r);
        }
    }

    private void executeKafkaPublish(PublishToKafka reaction) {
        log.info("Publishing to Kafka topic '{}': {}", reaction.topic(), reaction.message());
        kafkaTemplate.send(reaction.topic(), reaction.message());
    }

    private void executeFrontendPush(PushToFrontend reaction) {
        log.info("Pushing to frontend: {}", reaction.notification().message());
        for (Consumer<Notification> listener : frontendListeners) {
            try {
                listener.accept(reaction.notification());
            } catch (Exception e) {
                log.warn("Failed to push to frontend listener", e);
            }
        }
    }

    private void executeApiCall(CallExternalApi reaction) {
        log.info("Calling external API: {} {}", reaction.method(), reaction.url());
        try {
            restClient.method(org.springframework.http.HttpMethod.valueOf(reaction.method()))
                .uri(reaction.url())
                .body(reaction.body())
                .retrieve()
                .toBodilessEntity();
            log.info("API call successful: {}", reaction.url());
        } catch (Exception e) {
            log.error("API call failed: {} - {}", reaction.url(), e.getMessage());
        }
    }

    private void executeLogEvent(LogEvent reaction) {
        log.info("[{}] {} - Event: {}", reaction.level(), reaction.message(), reaction.event().id());
    }

    // Methods for SSE listener management
    public void addFrontendListener(Consumer<Notification> listener) {
        frontendListeners.add(listener);
    }

    public void removeFrontendListener(Consumer<Notification> listener) {
        frontendListeners.remove(listener);
    }
}
