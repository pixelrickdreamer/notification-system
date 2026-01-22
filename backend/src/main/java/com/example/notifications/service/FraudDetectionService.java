package com.example.notifications.service;

import com.example.notifications.Notification;
import com.example.notifications.entity.AuditLog;
import com.example.notifications.entity.FraudRule;
import com.example.notifications.entity.RuleAction;
import com.example.notifications.model.Application;
import com.example.notifications.repository.AuditLogRepository;
import com.example.notifications.repository.FraudRuleRepository;
import com.example.notifications.routing.ReactionExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final FraudRuleRepository ruleRepository;
    private final AuditLogRepository auditLogRepository;
    private final RuleEvaluator ruleEvaluator;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReactionExecutor reactionExecutor;
    private final ObjectMapper objectMapper;

    public FraudDetectionService(
            FraudRuleRepository ruleRepository,
            AuditLogRepository auditLogRepository,
            RuleEvaluator ruleEvaluator,
            KafkaTemplate<String, Object> kafkaTemplate,
            ReactionExecutor reactionExecutor) {
        this.ruleRepository = ruleRepository;
        this.auditLogRepository = auditLogRepository;
        this.ruleEvaluator = ruleEvaluator;
        this.kafkaTemplate = kafkaTemplate;
        this.reactionExecutor = reactionExecutor;
        this.objectMapper = new ObjectMapper();
    }

    public void processApplication(Application application) {
        log.info("Processing application: {} (type: {}, source: {})",
            application.id(), application.type(), application.sourceSystem());

        List<FraudRule> enabledRules = ruleRepository.findByEnabledTrueOrderByPriorityAsc();
        List<FraudRule> matchedRules = new ArrayList<>();

        // Evaluate all rules
        for (FraudRule rule : enabledRules) {
            if (ruleEvaluator.evaluate(rule, application)) {
                log.info("Rule '{}' matched for application {}", rule.getName(), application.id());
                matchedRules.add(rule);
            }
        }

        // Determine final action (highest priority matched rule wins)
        RuleAction finalAction = null;
        String actionDetails = null;

        if (!matchedRules.isEmpty()) {
            FraudRule primaryRule = matchedRules.get(0); // Highest priority (lowest number)
            finalAction = primaryRule.getActionType();
            actionDetails = primaryRule.getActionConfig();

            // Execute actions for all matched rules
            executeActions(application, matchedRules);
        } else {
            // No rules matched - application is clean
            log.info("Application {} passed all rules, routing to clean-apps", application.id());
            routeToTopic("clean-apps", application);
        }

        // Create audit log
        createAuditLog(application, enabledRules.size(), matchedRules, finalAction, actionDetails);
    }

    private void executeActions(Application application, List<FraudRule> matchedRules) {
        boolean blocked = false;

        for (FraudRule rule : matchedRules) {
            if (blocked) break;

            switch (rule.getActionType()) {
                case FLAG -> {
                    String reason = extractConfigValue(rule.getActionConfig(), "reason", "Flagged by " + rule.getName());
                    String severity = extractConfigValue(rule.getActionConfig(), "severity", "MEDIUM");

                    Notification notification = Notification.create(
                        "fraud-gateway",
                        severity.equalsIgnoreCase("HIGH") ? "error" : "warning",
                        String.format("Flagged: %s - %s", application.id(), reason)
                    );
                    reactionExecutor.execute(new com.example.notifications.model.Reaction.PushToFrontend(notification));

                    routeToTopic("flagged-apps", application, Map.of(
                        "flagReason", reason,
                        "severity", severity,
                        "ruleName", rule.getName()
                    ));
                }
                case BLOCK -> {
                    String reason = extractConfigValue(rule.getActionConfig(), "reason", "Blocked by " + rule.getName());

                    Notification notification = Notification.create(
                        "fraud-gateway",
                        "error",
                        String.format("Blocked: %s - %s", application.id(), reason)
                    );
                    reactionExecutor.execute(new com.example.notifications.model.Reaction.PushToFrontend(notification));

                    routeToTopic("blocked-apps", application, Map.of(
                        "blockReason", reason,
                        "ruleName", rule.getName()
                    ));
                    blocked = true;
                }
                case ROUTE -> {
                    String topic = extractConfigValue(rule.getActionConfig(), "topic", "manual-review");
                    routeToTopic(topic, application);
                }
                case ENRICH -> {
                    // For ENRICH, we add metadata but continue processing
                    log.info("Enriching application {} with rule {}", application.id(), rule.getName());
                }
            }
        }
    }

    private void routeToTopic(String topic, Application application) {
        routeToTopic(topic, application, Map.of());
    }

    private void routeToTopic(String topic, Application application, Map<String, Object> additionalData) {
        Map<String, Object> message = new HashMap<>(application.data());
        message.put("_applicationId", application.id());
        message.put("_processedAt", java.time.Instant.now().toString());
        message.putAll(additionalData);

        kafkaTemplate.send(topic, application.id(), message);
        log.info("Routed application {} to topic {}", application.id(), topic);
    }

    private void createAuditLog(Application application, int rulesEvaluated,
                                 List<FraudRule> matchedRules, RuleAction finalAction, String actionDetails) {
        AuditLog auditLog = new AuditLog();
        auditLog.setApplicationId(application.id());
        auditLog.setApplicationType(application.type());
        auditLog.setSourceSystem(application.sourceSystem());
        auditLog.setRulesEvaluated(rulesEvaluated);
        auditLog.setRulesMatched(matchedRules.size());
        auditLog.setMatchedRuleIds(matchedRules.stream()
            .map(r -> r.getId().toString())
            .collect(Collectors.joining(",")));
        auditLog.setMatchedRuleNames(matchedRules.stream()
            .map(FraudRule::getName)
            .collect(Collectors.joining(",")));
        auditLog.setFinalAction(finalAction);
        auditLog.setActionDetails(actionDetails);

        auditLogRepository.save(auditLog);
    }

    @SuppressWarnings("unchecked")
    private String extractConfigValue(String configJson, String key, String defaultValue) {
        if (configJson == null || configJson.isBlank()) {
            return defaultValue;
        }
        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            return config.getOrDefault(key, defaultValue).toString();
        } catch (JsonProcessingException e) {
            return defaultValue;
        }
    }
}
