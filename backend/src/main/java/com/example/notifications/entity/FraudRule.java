package com.example.notifications.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fraud_rules")
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int priority = 100;

    // Condition
    @Column(name = "field_path", nullable = false)
    private String fieldPath;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleOperator operator;

    @Column(name = "rule_value", nullable = false)
    private String value;

    // Action
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleAction actionType;

    @Column(name = "action_config", columnDefinition = "TEXT")
    private String actionConfig;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getFieldPath() { return fieldPath; }
    public void setFieldPath(String fieldPath) { this.fieldPath = fieldPath; }

    public RuleOperator getOperator() { return operator; }
    public void setOperator(RuleOperator operator) { this.operator = operator; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public RuleAction getActionType() { return actionType; }
    public void setActionType(RuleAction actionType) { this.actionType = actionType; }

    public String getActionConfig() { return actionConfig; }
    public void setActionConfig(String actionConfig) { this.actionConfig = actionConfig; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
