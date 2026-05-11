package com.fraudoperations.models;

/**
 * Represents a single fraud detection rule loaded from fraud_detection_rules.csv.
 *
 * Each rule defines a condition to check against a transaction and/or suspect,
 * the flag code to apply when the condition is met, and the risk score to add.
 *
 * The ruleEngine evaluates each FraudRule against every transaction at runtime.
 */
public class FraudRule {

    /** Rule identifier (e.g. TXN-021). */
    private String ruleID;

    /** Human-readable rule name. */
    private String ruleName;

    /** Category grouping (e.g. Elder Protection, Amount Threshold). */
    private String category;

    /** The field or fields this rule monitors. */
    private String fieldMonitored;

    /** Plain-English description of the condition. */
    private String condition;

    /** Comparison operator: >, >=, ==, !=, IN, BETWEEN. */
    private String operator;

    /** The threshold or comparison value as a string. */
    private String value;

    /**
     * The flag code applied to the transaction when this rule triggers.
     * Example: ELDER_ABUSE_INDICATOR, NAME_MISMATCH, AMOUNT_HIGH.
     */
    private String flagType;

    /** Risk score added when this rule triggers (1-5 scale). */
    private int riskScore;

    /** Recommended action when this rule triggers. */
    private String actionTriggered;

    /** True if this rule only applies to online (card-not-present) transactions. */
    private boolean onlineOnly;

    /** Additional notes about the rule for investigator reference. */
    private String notes;

    /**
     * Full constructor used by FraudRuleEngine when loading from CSV.
     *
     * @param ruleID          rule identifier
     * @param ruleName        human-readable name
     * @param category        rule category
     * @param fieldMonitored  fields this rule checks
     * @param condition       plain-English condition description
     * @param operator        comparison operator
     * @param value           threshold or comparison value
     * @param flagType        flag code to apply
     * @param riskScore       risk score contribution (1-5)
     * @param actionTriggered recommended action on trigger
     * @param onlineOnly      true if rule is online-only
     * @param notes           investigator notes
     */
    public FraudRule(String ruleID, String ruleName, String category,
                     String fieldMonitored, String condition, String operator,
                     String value, String flagType, int riskScore,
                     String actionTriggered, boolean onlineOnly, String notes) {
        this.ruleID          = ruleID;
        this.ruleName        = ruleName;
        this.category        = category;
        this.fieldMonitored  = fieldMonitored;
        this.condition       = condition;
        this.operator        = operator;
        this.value           = value;
        this.flagType        = flagType;
        this.riskScore       = riskScore;
        this.actionTriggered = actionTriggered;
        this.onlineOnly      = onlineOnly;
        this.notes           = notes;
    }

    // Getters

    /** @return the rule identifier */
    public String getRuleID()          { return ruleID; }

    /** @return the rule name */
    public String getRuleName()        { return ruleName; }

    /** @return the rule category */
    public String getCategory()        { return category; }

    /** @return the fields monitored by this rule */
    public String getFieldMonitored()  { return fieldMonitored; }

    /** @return the plain-English condition description */
    public String getCondition()       { return condition; }

    /** @return the comparison operator */
    public String getOperator()        { return operator; }

    /** @return the threshold value string */
    public String getValue()           { return value; }

    /** @return the flag code applied when this rule triggers */
    public String getFlagType()        { return flagType; }

    /** @return the risk score contribution */
    public int getRiskScore()          { return riskScore; }

    /** @return the recommended action */
    public String getActionTriggered() { return actionTriggered; }

    /** @return true if this rule only applies to online transactions */
    public boolean isOnlineOnly()      { return onlineOnly; }

    /** @return investigator notes */
    public String getNotes()           { return notes; }

    /**
     * Returns a formatted one-line summary of this rule.
     *
     * @return formatted rule string
     */
    @Override
    public String toString() {
        return String.format("[%s] %s | Flag: %s | Score: %d | Online Only: %s",
                ruleID, ruleName, flagType, riskScore, onlineOnly);
    }
}