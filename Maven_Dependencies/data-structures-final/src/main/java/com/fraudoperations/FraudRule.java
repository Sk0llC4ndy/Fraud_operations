package com.fraudoperations;

public class FraudRule {
    private String ruleId;
    private String ruleName;
    private String category;
    private String fieldMonitored;
    private String condition;
    private String operator;
    private String value;
    private String flagType;
    private int riskScore;        // 1–5
    private String actionTriggered;
    private boolean onlineOnly;
    private String notes;

    // Getters/setters or use a Record in Java 16+
}