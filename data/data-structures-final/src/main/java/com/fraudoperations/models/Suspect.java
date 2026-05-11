package com.fraudoperations.models;

import java.util.*;

/**
 * Represents a cardholder profile under investigation.
 *
 * System must be able to register each suspect's name, ID, email, phone number,
 * address, IP, and age. Information on available balance, riskScore, and
 * riskLevel must also be sustained in the program.
 *
 * Implements Comparable so suspects can be ordered by riskScore descending
 * using the custom merge sort in RiskEngine.
 *
 * Data structures used:
 *   ArrayList<Transaction> - transaction history (List / ArrayList)
 *   ArrayList<Dispute>     - dispute history (List / ArrayList)
 *   ArrayList<Integer>     - IDs of connected suspects (List / ArrayList)
 */
public class Suspect implements Comparable<Suspect> {

    /** Unique numeric identifier for this suspect. */
    private int suspectID;

    /** Full legal name. */
    private String name;

    /** Email address on file. */
    private String email;

    /** Phone number on file. */
    private String phone;

    /** Full mailing address including city, state, and ZIP. */
    private String address;

    /** Most recent IP address associated with this suspect's account. */
    private String ip;

    /** Age in years. Used by isElder() to trigger elder abuse checks. */
    private int age;

    /** Current available account balance. */
    private double availableBalance;

    /**
     * Weighted risk score on a 1.0 to 5.0 scale.
     * Calculated by RiskEngine.calculateRiskScore().
     */
    private double riskScore;

    /**
     * Human-readable risk band.
     * One of: LOW, MEDIUM, HIGH.
     */
    private String riskLevel;

    /**
     * All transactions linked to this suspect.
     * Backed by an ArrayList.
     */
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * All disputes linked to this suspect.
     * Backed by an ArrayList.
     */
    private List<Dispute> disputes = new ArrayList<>();

    /**
     * IDs of suspects connected to this one via shared identifiers.
     * Populated by ConnectionGraph.buildConnectionGraph().
     * Backed by an ArrayList.
     */
    private List<Integer> connections = new ArrayList<>();

    /**
     * Full constructor used by CSVLoader.
     *
     * @param suspectID        unique identifier
     * @param name             full legal name
     * @param email            email address on file
     * @param phone            phone number
     * @param address          full mailing address
     * @param ip               most recent IP address
     * @param age              age in years
     * @param availableBalance current account balance
     */
    public Suspect(int suspectID, String name, String email, String phone,
                   String address, String ip, int age, double availableBalance) {
        this.suspectID        = suspectID;
        this.name             = name;
        this.email            = email;
        this.phone            = phone;
        this.address          = address;
        this.ip               = ip;
        this.age              = age;
        this.availableBalance = availableBalance;
        this.riskScore        = 0.0;
        this.riskLevel        = "LOW";
    }

    /**
     * Returns true if this cardholder is 65 years of age or older.
     * Used to activate elder-specific fraud rules including
     * ELDER_ABUSE_INDICATOR and CARETAKER_PATTERN.
     *
     * @return true if age is 65 or greater
     */
    public boolean isElder() {
        return age >= 65;
    }

    /**
     * Compares this suspect to another by risk score in descending order.
     * Required by the custom merge sort in RiskEngine.sortSuspectsByRisk().
     *
     * @param other the suspect to compare against
     * @return negative if this suspect has higher risk, positive if lower
     */
    @Override
    public int compareTo(Suspect other) {
        return Double.compare(other.riskScore, this.riskScore);
    }

    // Getters and Setters

    /** @return the unique suspect ID */
    public int getSuspectID() {
        return suspectID;
    }
    /** @param suspectID the new suspect ID */
    public void setSuspectID(int suspectID) {
        this.suspectID = suspectID;
    }

    /** @return the suspect's full name */
    public String getName() {
        return name;
    }
    /** @param name the updated name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the suspect's email address */
    public String getEmail() {
        return email;
    }
    /** @param email the updated email */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the suspect's phone number */
    public String getPhone() {
        return phone;
    }
    /** @param phone the updated phone number */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return the suspect's full mailing address */
    public String getAddress() {
        return address;
    }
    /** @param address the updated address */
    public void setAddress(String address) {
        this.address = address;
    }

    /** @return the suspect's most recent IP address */
    public String getIpAddress() {
        return ip;
    }
    /** @param ip the updated IP address */
    public void setIpAddress(String ip) {
        this.ip = ip;
    }

    /** @return the suspect's age in years */
    public int getAge() {
        return age;
    }
    /** @param age the updated age */
    public void setAge(int age) {
        this.age = age;
    }

    /** @return the current available account balance */
    public double getAvailableBalance() {
        return availableBalance;
    }
    /** @param availableBalance the updated balance */
    public void setAvailableBalance(double availableBalance) {
        this.availableBalance = availableBalance;
    }

    /** @return the calculated risk score (1.0 to 5.0) */
    public double getRiskScore() {
        return riskScore;
    }
    /** @param riskScore the new risk score */
    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    /** @return the risk level string: LOW, MEDIUM, or HIGH */
    public String getRiskLevel() {
        return riskLevel;
    }
    /** @param riskLevel the new risk level */
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    /** @return the ArrayList of all transactions linked to this suspect */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /** @return the ArrayList of all disputes linked to this suspect */
    public List<Dispute> getDisputes() {
        return disputes;
    }

    /** @return the ArrayList of suspect IDs connected to this suspect */
    public List<Integer> getConnections() {
        return connections;
    }

    /**
     * Appends a transaction to this suspect's transaction ArrayList.
     *
     * @param t the transaction to add
     */
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    /**
     * Appends a dispute to this suspect's dispute ArrayList.
     *
     * @param d the dispute to add
     */
    public void addDispute(Dispute d) {
        disputes.add(d);
    }

    /**
     * Adds a connected suspect ID to the connections ArrayList
     * if it is not already present.
     *
     * @param suspectID the ID of the suspect to connect
     */
    public void addConnection(int suspectID) {
        if (!connections.contains(suspectID)) {
            connections.add(suspectID);
        }
    }

    /**
     * Returns a formatted summary of this suspect including elder status and risk.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return String.format("[%d] %s | Age: %d%s | Risk: %.1f (%s) | IP: %s",
                suspectID, name, age, isElder() ? " [ELDER]" : "",
                riskScore, riskLevel, ip);
    }
}