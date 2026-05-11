package com.fraudoperations.systems;

import com.fraudoperations.models.FraudRule;
import com.fraudoperations.models.Suspect;
import com.fraudoperations.models.Transaction;

import java.io.*;
import java.util.*;

/**
 * Feature 11 - Fraud Rule Engine
 *
 * Loads all 28 fraud detection rules from fraud_detection_rules.csv at startup
 * and evaluates each rule against every transaction and suspect in the database.
 *
 * Rules are stored in an ArrayList<FraudRule>. Each rule's flagType is applied
 * to a transaction when its condition is met. Elder protection rules (TXN-021
 * through TXN-028) are automatically triggered for cardholders aged 65 or older.
 *
 * How it connects to the rest of the program:
 *   - Called from Main after CSVLoader links transactions to suspects
 *   - Writes flag codes to Transaction.flagReason (same format as TransactionManager)
 *   - Results feed into RiskEngine scoring and ConnectionGraph detection
 *
 * Data structures used:
 *   ArrayList<FraudRule>  - ordered list of all loaded rules
 *   ArrayList<String>     - internal CSV parser
 */
public class FraudRuleEngine {

    /** All loaded fraud rules. Backed by an ArrayList. */
    private final List<FraudRule> rules = new ArrayList<>();

    /** Path to the fraud_detection_rules.csv file. */
    private final String rulesPath;

    /**
     * Constructs a FraudRuleEngine pointing at the given CSV file.
     *
     * @param rulesPath path to fraud_detection_rules.csv
     */
    public FraudRuleEngine(String rulesPath) {
        this.rulesPath = rulesPath;
    }

    // ── Load Rules from CSV ───────────────────────────────────────────────────

    /**
     * Reads fraud_detection_rules.csv and populates the rules ArrayList.
     * Skips the header row automatically.
     * Called once at startup before evaluateAll().
     */
    public void loadRules() {
        rules.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(rulesPath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] col = parseLine(line);
                if (col.length < 12) {
                    continue;
                }

                String ruleID          = col[0].trim();
                String ruleName        = col[1].trim();
                String category        = col[2].trim();
                String fieldMonitored  = col[3].trim();
                String condition       = col[4].trim();
                String operator        = col[5].trim();
                String value           = col[6].trim();
                String flagType        = col[7].trim();
                int    riskScore       = parseIntSafe(col[8].trim());
                String actionTriggered = col[9].trim();
                boolean onlineOnly     = col[10].trim().equalsIgnoreCase("Yes");
                String notes           = col[11].trim();

                FraudRule rule = new FraudRule(ruleID, ruleName, category,
                        fieldMonitored, condition, operator, value,
                        flagType, riskScore, actionTriggered, onlineOnly, notes);
                rules.add(rule);
            }

        } catch (IOException e) {
            System.err.println("ERROR loading fraud rules CSV: " + e.getMessage());
        }

        System.out.println("Loaded " + rules.size() + " fraud rules from " + rulesPath);
    }

    // ── Evaluate All Suspects ─────────────────────────────────────────────────

    /**
     * Iterates every suspect and evaluates all applicable rules against
     * each of their transactions. Elder protection rules are applied
     * automatically when the cardholder is 65 or older.
     *
     * @param suspectDB the master suspect Map from CSVLoader
     */
    public void evaluateAll(Map<Integer, Suspect> suspectDB) {
        if (rules.isEmpty()) {
            System.out.println("WARNING: No rules loaded. Call loadRules() first.");
            return;
        }

        int flagsApplied = 0;

        for (Suspect fraudster : suspectDB.values()) {
            List<Transaction> transactions = fraudster.getTransactions();
            for (int i = 0; i < transactions.size(); i++) {
                Transaction fraud = transactions.get(i);
                flagsApplied += evaluateTransaction(fraud, fraudster);
            }
        }

        System.out.println("Rule evaluation complete. Total new flags applied: " + flagsApplied);
    }

    /**
     * Evaluates all loaded rules against a single transaction and suspect pair.
     * Skips online-only rules for in-person transactions.
     * Applies the rule's flagType if the condition is met.
     *
     * @param fraud     the transaction to evaluate
     * @param fraudster the suspect who owns this transaction
     * @return the number of new flags applied to this transaction
     */
    public int evaluateTransaction(Transaction fraud, Suspect fraudster) {
        int flagsApplied = 0;

        for (int i = 0; i < rules.size(); i++) {
            FraudRule rule = rules.get(i);

            // Skip online-only rules for in-person transactions
            if (rule.isOnlineOnly() && !fraud.isOnline()) {
                continue;
            }

            // Check if the flag is already present — don't double-flag
            if (alreadyFlagged(fraud, rule.getFlagType())) {
                continue;
            }

            // Evaluate the rule condition
            boolean triggered = checkRule(rule, fraud, fraudster);

            if (triggered) {
                applyFlag(fraud, rule.getFlagType());
                flagsApplied++;

                System.out.println("  [" + rule.getRuleID() + "] " + rule.getRuleName()
                        + " triggered on TXN-" + fraud.getTransactionID()
                        + " (Suspect " + fraudster.getSuspectID()
                        + " - " + fraudster.getName() + ")"
                        + " | Action: " + rule.getActionTriggered());
            }
        }

        return flagsApplied;
    }

    // ── Rule Condition Checker ────────────────────────────────────────────────

    /**
     * Evaluates a single rule's condition against the given transaction and suspect.
     * Each ruleID maps to a specific programmatic check.
     *
     * @param rule      the rule to evaluate
     * @param fraud     the transaction being checked
     * @param fraudster the suspect who owns the transaction
     * @return true if the rule condition is met
     */
    private boolean checkRule(FraudRule rule, Transaction fraud, Suspect fraudster) {
        switch (rule.getRuleID()) {

            // ── Standard Transaction Rules ────────────────────────────────────

            case "TXN-001":
                // Amount > $5000
                return fraud.getAmount() > 5000.0;

            case "TXN-003":
                // Round number — multiple of $500
                return fraud.getAmount() > 0 && fraud.getAmount() % 500 == 0;

            case "TXN-004":
                // Online + new device (IP not matching suspect's on-file IP)
                if (!fraud.isOnline()) {
                    return false;
                }
                return !fraud.getIpAddress().equals(fraudster.getIpAddress());

            case "TXN-006":
                // Billing address != shipping address (online only)
                return !fraud.getBillingAddress().equalsIgnoreCase(fraud.getShippingAddress());

            case "TXN-009":
                // Transaction between 12 AM and 5 AM
                return isOddHour(fraud.getTime(), 0, 5);

            case "TXN-010":
                // Online transaction > $1500
                return fraud.isOnline() && fraud.getAmount() > 1500.0;

            case "TXN-011":
                // More than 2 failed auth attempts
                return fraud.getAuthAttempts() > 2;

            case "TXN-014":
                // Name on transaction != cardholder name
                return !fraud.getNameOnTransaction().equalsIgnoreCase(fraudster.getName());

            case "TXN-015":
                // Duplicate: same amount at same merchant as another transaction
                return isDuplicate(fraud, fraudster);

            case "TXN-019":
                // Unusual merchant category — first time seen for this suspect
                return isUnusualMerchant(fraud, fraudster);

            // ── Elder Protection Rules ────────────────────────────────────────

            case "TXN-021":
                // Elder cardholder — age >= 65 with any flagged activity
                return fraudster.isElder();

            case "TXN-022":
                // Caretaker pattern — different name but same address as elder
                if (!fraudster.isElder()) {
                    return false;
                }
                return !fraud.getNameOnTransaction().equalsIgnoreCase(fraudster.getName());

            case "TXN-023":
                // Dispute filed by someone other than the cardholder
                if (!fraudster.isElder()) {
                    return false;
                }
                return fraud.isThirdPartyDispute();

            case "TXN-024":
                // Elder with more than 2 disputes on file
                if (!fraudster.isElder()) {
                    return false;
                }
                return fraudster.getDisputes().size() > 2;

            case "TXN-025":
                // Elder account transaction between 10 PM and 6 AM
                if (!fraudster.isElder()) {
                    return false;
                }
                return isOddHour(fraud.getTime(), 22, 6);

            case "TXN-026":
                // Cash equivalent on elder account
                if (!fraudster.isElder()) {
                    return false;
                }
                return isCashEquivalent(fraud.getMerchantCategory());

            case "TXN-027":
                // Email mismatch on elder account
                if (!fraudster.isElder()) {
                    return false;
                }
                return !fraud.getEmailOnTransaction().equalsIgnoreCase(fraudster.getEmail());

            case "TXN-028":
                // Elder account ships to non-cardholder address
                if (!fraudster.isElder() || !fraud.isOnline()) {
                    return false;
                }
                return !fraud.getShippingAddress().equalsIgnoreCase(fraudster.getAddress());

            // Rules handled by TransactionManager or needing external data
            // (TXN-002 velocity, TXN-005 geo, TXN-007 multi-ship,
            //  TXN-008 freight, TXN-012 account age, TXN-013 blacklist,
            //  TXN-016 high-risk country, TXN-017 merchant velocity,
            //  TXN-018 daily limit, TXN-020 composite risk)
            // These are noted here for completeness — extend as needed
            default:
                return false;
        }
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    /**
     * Returns true if the given flag code is already in the transaction's
     * pipe-delimited flagReason string.
     *
     * @param fraud    the transaction to check
     * @param flagType the flag code to look for
     * @return true if already flagged with this code
     */
    private boolean alreadyFlagged(Transaction fraud, String flagType) {
        String existing = fraud.getFlagReason();
        if (existing == null || existing.isBlank()) {
            return false;
        }
        String[] parts = existing.split("\\|");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].trim().equals(flagType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Appends a flag code to the transaction's pipe-delimited flagReason string
     * and marks it as flagged.
     *
     * @param fraud    the transaction to flag
     * @param flagType the flag code to append
     */
    private void applyFlag(Transaction fraud, String flagType) {
        fraud.setFlagged(true);
        String existing = fraud.getFlagReason();
        if (existing == null || existing.isBlank()) {
            fraud.setFlagReason(flagType);
        } else {
            fraud.setFlagReason(existing + "|" + flagType);
        }
    }

    /**
     * Returns true if the transaction time falls within the given hour range.
     * Handles overnight ranges (e.g. 22:00 to 06:00).
     *
     * @param time      time string in HH:MM format
     * @param startHour start of the flagged window (inclusive)
     * @param endHour   end of the flagged window (exclusive)
     * @return true if the transaction falls in the window
     */
    private boolean isOddHour(String time, int startHour, int endHour) {
        try {
            int hour = Integer.parseInt(time.split(":")[0]);
            if (startHour < endHour) {
                // Normal range e.g. 0 to 5
                return hour >= startHour && hour < endHour;
            } else {
                // Overnight range e.g. 22 to 6
                return hour >= startHour || hour < endHour;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if a transaction with the same amount and merchant ID
     * already exists in the suspect's transaction history.
     *
     * @param fraud     the transaction to check
     * @param fraudster the owning suspect
     * @return true if a duplicate is found
     */
    private boolean isDuplicate(Transaction fraud, Suspect fraudster) {
        List<Transaction> all = fraudster.getTransactions();
        for (int i = 0; i < all.size(); i++) {
            Transaction other = all.get(i);
            if (other.getTransactionID() == fraud.getTransactionID()) {
                continue; // skip self
            }
            if (other.getAmount() == fraud.getAmount()
                    && other.getMerchantID().equals(fraud.getMerchantID())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the merchant category is a cash-equivalent instrument.
     * Used by TXN-026 for elder abuse detection.
     *
     * @param merchantCategory the merchant category from the transaction
     * @return true if wire transfer, gift card, or cash advance
     */
    private boolean isCashEquivalent(String merchantCategory) {
        if (merchantCategory == null) {
            return false;
        }
        String cat = merchantCategory.toUpperCase();
        return cat.equals("WIRE_TRANSFER")
                || cat.equals("GIFT_CARD")
                || cat.equals("CASH_ADVANCE");
    }

    /**
     * Returns true if the transaction's merchant category has never appeared
     * in the suspect's previous transaction history.
     * Used by TXN-019 unusual merchant category detection.
     *
     * @param fraud     the transaction to check
     * @param fraudster the owning suspect
     * @return true if this is the first transaction in this merchant category
     */
    private boolean isUnusualMerchant(Transaction fraud, Suspect fraudster) {
        String category = fraud.getMerchantCategory();
        if (category == null || category.isBlank()) {
            return false;
        }
        List<Transaction> all = fraudster.getTransactions();
        int count = 0;
        for (int i = 0; i < all.size(); i++) {
            if (category.equalsIgnoreCase(all.get(i).getMerchantCategory())) {
                count++;
            }
        }
        // Only flag if this is the first and only transaction in this category
        return count <= 1;
    }

    // ── Display & Lookup ──────────────────────────────────────────────────────

    /**
     * Prints all loaded rules to the console in a formatted table.
     */
    public void displayAllRules() {
        System.out.println("═".repeat(80));
        System.out.println("  FRAUD DETECTION RULES  (" + rules.size() + " loaded)");
        System.out.println("═".repeat(80));
        System.out.printf("  %-10s %-38s %-22s %-5s%n",
                "Rule ID", "Rule Name", "Flag Type", "Score");
        System.out.println("─".repeat(80));
        for (int i = 0; i < rules.size(); i++) {
            FraudRule rule = rules.get(i);
            System.out.printf("  %-10s %-38s %-22s %-5d%n",
                    rule.getRuleID(),
                    rule.getRuleName(),
                    rule.getFlagType(),
                    rule.getRiskScore());
        }
        System.out.println("═".repeat(80));
    }

    /**
     * Prints only the elder protection rules (category = Elder Protection).
     */
    public void displayElderRules() {
        System.out.println("═".repeat(80));
        System.out.println("  ELDER PROTECTION RULES");
        System.out.println("═".repeat(80));
        for (int i = 0; i < rules.size(); i++) {
            FraudRule rule = rules.get(i);
            if ("Elder Protection".equalsIgnoreCase(rule.getCategory())) {
                System.out.println("  " + rule);
                System.out.println("    Condition : " + rule.getCondition());
                System.out.println("    Action    : " + rule.getActionTriggered());
                System.out.println("    Notes     : " + rule.getNotes());
                System.out.println();
            }
        }
        System.out.println("═".repeat(80));
    }

    /**
     * Returns the full rules ArrayList for external use.
     *
     * @return ArrayList of all loaded FraudRule objects
     */
    public List<FraudRule> getRules() {
        return rules;
    }

    /**
     * Returns a single rule by its ruleID, or null if not found.
     *
     * @param ruleID the rule identifier to search for
     * @return the matching FraudRule, or null
     */
    public FraudRule getRuleByID(String ruleID) {
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getRuleID().equalsIgnoreCase(ruleID)) {
                return rules.get(i);
            }
        }
        return null;
    }

    // ── CSV Parser ────────────────────────────────────────────────────────────

    /**
     * Parses a single CSV line, handling quoted fields that contain commas.
     *
     * @param line the raw CSV line
     * @return array of field values
     */
    private String[] parseLine(String line) {
        List<String> result   = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes      = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Safely parses an integer, returning 0 on failure.
     *
     * @param s the string to parse
     * @return parsed integer, or 0 if invalid
     */
    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}