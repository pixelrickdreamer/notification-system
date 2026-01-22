package com.example.notifications.service;

import com.example.notifications.entity.FraudRule;
import com.example.notifications.entity.RuleOperator;
import com.example.notifications.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RuleEvaluator {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluator.class);

    public boolean evaluate(FraudRule rule, Application application) {
        Object fieldValue = application.getFieldValue(rule.getFieldPath());
        String ruleValue = rule.getValue();
        RuleOperator operator = rule.getOperator();

        log.debug("Evaluating rule '{}': field '{}' = '{}', operator {}, expected '{}'",
            rule.getName(), rule.getFieldPath(), fieldValue, operator, ruleValue);

        try {
            return switch (operator) {
                case EQUALS -> equals(fieldValue, ruleValue);
                case NOT_EQUALS -> !equals(fieldValue, ruleValue);
                case CONTAINS -> contains(fieldValue, ruleValue);
                case NOT_CONTAINS -> !contains(fieldValue, ruleValue);
                case GREATER_THAN -> compareNumeric(fieldValue, ruleValue) > 0;
                case LESS_THAN -> compareNumeric(fieldValue, ruleValue) < 0;
                case GREATER_THAN_OR_EQUALS -> compareNumeric(fieldValue, ruleValue) >= 0;
                case LESS_THAN_OR_EQUALS -> compareNumeric(fieldValue, ruleValue) <= 0;
                case REGEX -> matchesRegex(fieldValue, ruleValue);
                case IN_LIST -> inList(fieldValue, ruleValue);
                case NOT_IN_LIST -> !inList(fieldValue, ruleValue);
                case IS_NULL -> fieldValue == null;
                case IS_NOT_NULL -> fieldValue != null;
            };
        } catch (Exception e) {
            log.warn("Error evaluating rule '{}': {}", rule.getName(), e.getMessage());
            return false;
        }
    }

    private boolean equals(Object fieldValue, String ruleValue) {
        if (fieldValue == null) {
            return ruleValue == null || ruleValue.equalsIgnoreCase("null");
        }
        return fieldValue.toString().equals(ruleValue);
    }

    private boolean contains(Object fieldValue, String ruleValue) {
        if (fieldValue == null) {
            return false;
        }
        return fieldValue.toString().toLowerCase().contains(ruleValue.toLowerCase());
    }

    private int compareNumeric(Object fieldValue, String ruleValue) {
        if (fieldValue == null) {
            throw new IllegalArgumentException("Field value is null");
        }

        double fieldNum;
        if (fieldValue instanceof Number) {
            fieldNum = ((Number) fieldValue).doubleValue();
        } else {
            fieldNum = Double.parseDouble(fieldValue.toString());
        }

        double ruleNum = Double.parseDouble(ruleValue);
        return Double.compare(fieldNum, ruleNum);
    }

    private boolean matchesRegex(Object fieldValue, String pattern) {
        if (fieldValue == null) {
            return false;
        }
        return Pattern.matches(pattern, fieldValue.toString());
    }

    private boolean inList(Object fieldValue, String ruleValue) {
        if (fieldValue == null) {
            return false;
        }
        // Rule value is comma-separated list
        List<String> values = Arrays.asList(ruleValue.split(","));
        return values.stream()
            .map(String::trim)
            .anyMatch(v -> v.equalsIgnoreCase(fieldValue.toString()));
    }
}
