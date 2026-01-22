package com.example.notifications.routing;

import com.example.notifications.model.Application;
import com.example.notifications.model.IncomingEvent;
import com.example.notifications.model.Reaction;
import com.example.notifications.service.FraudDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.Map;

@Service
public class EventRouter {

    private static final Logger log = LoggerFactory.getLogger(EventRouter.class);

    private final List<RoutingRule> rules;
    private final ReactionExecutor reactionExecutor;
    private final FraudDetectionService fraudDetectionService;

    public EventRouter(List<RoutingRule> rules, ReactionExecutor reactionExecutor,
                       FraudDetectionService fraudDetectionService) {
        this.rules = rules;
        this.reactionExecutor = reactionExecutor;
        this.fraudDetectionService = fraudDetectionService;
        log.info("EventRouter initialized with {} code-based rules: {}",
            rules.size(),
            rules.stream().map(RoutingRule::getName).toList());
    }

    @KafkaListener(
        topics = "applications.events",
        groupId = "fraud-gateway",
        properties = {
            "spring.json.trusted.packages=*",
            "spring.json.value.default.type=java.util.Map"
        }
    )
    public void processApplication(ConsumerRecord<String, Map<String, Object>> record) {
        String topic = record.topic();
        Map<String, Object> payload = record.value();

        log.info("Received application on topic {}: {}", topic, payload);

        Application application = Application.fromKafkaMessage(topic, payload);
        fraudDetectionService.processApplication(application);
    }

    @KafkaListener(
        topicPattern = "(?!applications).*\\.events",
        groupId = "event-router",
        properties = {
            "spring.json.trusted.packages=*",
            "spring.json.value.default.type=java.util.Map"
        }
    )
    public void routeEvent(ConsumerRecord<String, Map<String, Object>> record) {
        String topic = record.topic();
        Map<String, Object> payload = record.value();

        log.info("Received event on topic {}: {}", topic, payload);

        // Extract event type from payload
        String type = (String) payload.get("type");
        String source = (String) payload.getOrDefault("source", "unknown");

        if (type == null) {
            log.warn("Event missing 'type' field, skipping: {}", payload);
            return;
        }

        IncomingEvent event = IncomingEvent.create(source, topic, type, payload);

        // Find matching rules and execute reactions (code-based rules)
        int matchCount = 0;
        for (RoutingRule rule : rules) {
            if (rule.matches(event)) {
                log.info("Rule '{}' matched event {}", rule.getName(), event.id());
                matchCount++;

                List<Reaction> reactions = rule.getReactions(event);
                for (Reaction reaction : reactions) {
                    reactionExecutor.execute(reaction);
                }
            }
        }

        if (matchCount == 0) {
            log.debug("No rules matched event: {} (type={})", event.id(), type);
        }
    }
}
