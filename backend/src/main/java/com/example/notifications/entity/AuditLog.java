package com.example.notifications.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "application_type")
    private String applicationType;

    @Column(name = "source_system")
    private String sourceSystem;

    @Column(name = "rules_evaluated")
    private int rulesEvaluated;

    @Column(name = "rules_matched")
    private int rulesMatched;

    @Column(name = "matched_rule_ids", columnDefinition = "TEXT")
    private String matchedRuleIds;

    @Column(name = "matched_rule_names", columnDefinition = "TEXT")
    private String matchedRuleNames;

    @Column(name = "final_action")
    @Enumerated(EnumType.STRING)
    private RuleAction finalAction;

    @Column(name = "action_details", columnDefinition = "TEXT")
    private String actionDetails;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getApplicationType() { return applicationType; }
    public void setApplicationType(String applicationType) { this.applicationType = applicationType; }

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public int getRulesEvaluated() { return rulesEvaluated; }
    public void setRulesEvaluated(int rulesEvaluated) { this.rulesEvaluated = rulesEvaluated; }

    public int getRulesMatched() { return rulesMatched; }
    public void setRulesMatched(int rulesMatched) { this.rulesMatched = rulesMatched; }

    public String getMatchedRuleIds() { return matchedRuleIds; }
    public void setMatchedRuleIds(String matchedRuleIds) { this.matchedRuleIds = matchedRuleIds; }

    public String getMatchedRuleNames() { return matchedRuleNames; }
    public void setMatchedRuleNames(String matchedRuleNames) { this.matchedRuleNames = matchedRuleNames; }

    public RuleAction getFinalAction() { return finalAction; }
    public void setFinalAction(RuleAction finalAction) { this.finalAction = finalAction; }

    public String getActionDetails() { return actionDetails; }
    public void setActionDetails(String actionDetails) { this.actionDetails = actionDetails; }

    public Instant getProcessedAt() { return processedAt; }
}
