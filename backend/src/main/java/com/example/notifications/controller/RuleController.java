package com.example.notifications.controller;

import com.example.notifications.entity.FraudRule;
import com.example.notifications.entity.RuleAction;
import com.example.notifications.entity.RuleOperator;
import com.example.notifications.repository.FraudRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "http://localhost:5173")
public class RuleController {

    private final FraudRuleRepository ruleRepository;

    public RuleController(FraudRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public List<FraudRule> getAllRules() {
        return ruleRepository.findAllByOrderByPriorityAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FraudRule> getRule(@PathVariable Long id) {
        return ruleRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FraudRule createRule(@RequestBody FraudRule rule) {
        rule.setId(null); // Ensure new entity
        return ruleRepository.save(rule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FraudRule> updateRule(@PathVariable Long id, @RequestBody FraudRule rule) {
        return ruleRepository.findById(id)
            .map(existing -> {
                existing.setName(rule.getName());
                existing.setDescription(rule.getDescription());
                existing.setEnabled(rule.isEnabled());
                existing.setPriority(rule.getPriority());
                existing.setFieldPath(rule.getFieldPath());
                existing.setOperator(rule.getOperator());
                existing.setValue(rule.getValue());
                existing.setActionType(rule.getActionType());
                existing.setActionConfig(rule.getActionConfig());
                return ResponseEntity.ok(ruleRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (ruleRepository.existsById(id)) {
            ruleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<FraudRule> toggleRule(@PathVariable Long id) {
        return ruleRepository.findById(id)
            .map(rule -> {
                rule.setEnabled(!rule.isEnabled());
                return ResponseEntity.ok(ruleRepository.save(rule));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/operators")
    public List<Map<String, String>> getOperators() {
        return Arrays.stream(RuleOperator.values())
            .map(op -> Map.of(
                "value", op.name(),
                "label", formatOperatorLabel(op)
            ))
            .toList();
    }

    @GetMapping("/actions")
    public List<Map<String, String>> getActions() {
        return Arrays.stream(RuleAction.values())
            .map(action -> Map.of(
                "value", action.name(),
                "label", formatActionLabel(action)
            ))
            .toList();
    }

    private String formatOperatorLabel(RuleOperator op) {
        return switch (op) {
            case EQUALS -> "Equals";
            case NOT_EQUALS -> "Not Equals";
            case CONTAINS -> "Contains";
            case NOT_CONTAINS -> "Does Not Contain";
            case GREATER_THAN -> "Greater Than";
            case LESS_THAN -> "Less Than";
            case GREATER_THAN_OR_EQUALS -> "Greater Than or Equals";
            case LESS_THAN_OR_EQUALS -> "Less Than or Equals";
            case REGEX -> "Matches Regex";
            case IN_LIST -> "In List";
            case NOT_IN_LIST -> "Not In List";
            case IS_NULL -> "Is Null";
            case IS_NOT_NULL -> "Is Not Null";
        };
    }

    private String formatActionLabel(RuleAction action) {
        return switch (action) {
            case FLAG -> "Flag for Review";
            case BLOCK -> "Block Application";
            case ROUTE -> "Route to Topic";
            case ENRICH -> "Enrich with Metadata";
        };
    }
}
