package com.fraudoperations.systems;

import com.fraudoperations.models.Dispute;
import com.fraudoperations.models.Suspect;
import com.fraudoperations.models.Transaction;

import java.util.*;

/**
 * Feature 8 - Risk Scoring and Sorting
 * Calculates weighted risk scores per suspect and sorts by priority.
 *
 * Data structures used:
 *   ArrayList<Suspect> - sortable list built from the suspect Map values
 *   Map<Integer,Suspect> - reference to the master suspect database
 *
 * Comparable implementation:
 *   Suspect implements Comparable<Suspect> ordering by riskScore descending.
 *   sortSuspectsByRisk() uses a custom merge sort that calls compareTo()
 *   to order suspects from highest to lowest risk.
 */
public class RiskEngine {

    /** Reference to the master suspect database. */
    private final Map<Integer, Suspect> suspectDB;

    /** Reference to the hash index system for shared identifier detection. */
    private final HashIndexSystem hashIndex;

    // Weight constants - adjust to tune sensitivity
    private static final double WEIGHT_FLAGGED_TXN   = 1.5;
    private static final double WEIGHT_DISPUTE       = 1.0;
    private static final double WEIGHT_LOSS          = 2.0;
    private static final double WEIGHT_CONNECTION    = 0.5;
    private static final double WEIGHT_SHARED_IP     = 3.0;
    private static final double WEIGHT_SHARED_EMAIL  = 2.0;
    private static final double WEIGHT_SHARED_ADDRESS = 2.5;

    // Risk level thresholds on the 1-5 scale
    // Above 2.0 = MEDIUM, Above 3.5 = HIGH
    private static final double HIGH_THRESHOLD   = 3.5;
    private static final double MEDIUM_THRESHOLD = 2.0;

    /**
     * Constructs a RiskEngine backed by the shared suspect database and hash index.
     *
     * @param suspectDB the master suspect Map
     * @param hashIndex the shared hash index system
     */
    public RiskEngine(Map<Integer, Suspect> suspectDB, HashIndexSystem hashIndex) {
        this.suspectDB = suspectDB;
        this.hashIndex = hashIndex;
    }

    /**
     * Calculates a weighted risk score for the given suspect and stores
     * the result back on the Suspect object.
     *
     * Factors:
     *   - Flagged transaction count (x1.5 each)
     *   - Dispute count (x1.0 each)
     *   - Loss count (x2.0 each)
     *   - Connection count (x0.5 each)
     *   - Shared IP (+3.0)
     *   - Shared email (+2.0)
     *   - Shared address (+2.5)
     *
     * Score is normalized to a 1.0-5.0 scale.
     *
     * @param fraudster the suspect to score
     * @return the calculated risk score
     */
    public double calculateRiskScore(Suspect fraudster) {
        double score = 0.0;

        // Count flagged transactions using a for loop
        long flaggedCount = 0;
        List<Transaction> transactions = fraudster.getTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            Transaction fraud = transactions.get(i);
            if (fraud.isFlagged()) {
                flaggedCount++;
            }
        }
        score += flaggedCount * WEIGHT_FLAGGED_TXN;

        // Count disputes
        int disputeCount = fraudster.getDisputes().size();
        score += disputeCount * WEIGHT_DISPUTE;

        // Count losses using a for loop
        int lossCount = 0;
        List<Dispute> disputes = fraudster.getDisputes();
        for (int i = 0; i < disputes.size(); i++) {
            Dispute dsp = disputes.get(i);
            if ("LOSS".equalsIgnoreCase(dsp.getResolution())) {
                lossCount++;
            }
        }
        score += lossCount * WEIGHT_LOSS;

        // Count connections (fraud ring membership)
        int connectionCount = fraudster.getConnections().size();
        score += connectionCount * WEIGHT_CONNECTION;

        // Check for shared identifiers
        if (hashIndex.isSharedIP(fraudster.getIpAddress())) {
            score += WEIGHT_SHARED_IP;
        }
        if (hashIndex.isSharedEmail(fraudster.getEmail())) {
            score += WEIGHT_SHARED_EMAIL;
        }
        if (hashIndex.isSharedAddress(fraudster.getAddress())) {
            score += WEIGHT_SHARED_ADDRESS;
        }

        // Normalize to 1-5 scale
        double normalized = 1.0 + Math.min(4.0, score / 5.0);
        double rounded    = Math.round(normalized * 10.0) / 10.0;

        fraudster.setRiskScore(rounded);
        fraudster.setRiskLevel(getRiskLevel(fraudster.getRiskScore()));

        return fraudster.getRiskScore();
    }

    /**
     * Returns the risk band label for a given score.
     *
     * @param score the risk score (1.0-5.0)
     * @return "HIGH", "MEDIUM", or "LOW"
     */
    public String getRiskLevel(double score) {
        if (score >= HIGH_THRESHOLD) {
            return "HIGH";
        }
        if (score >= MEDIUM_THRESHOLD) {
            return "MEDIUM";
        }
        return "LOW";
    }

    /**
     * Calls calculateRiskScore() on every suspect in the database.
     */
    public void scoreAllSuspects() {
        Collection<Suspect> suspects = suspectDB.values();
        for (Suspect fraudster : suspects) {
            calculateRiskScore(fraudster);
        }
        System.out.println("Risk scores calculated for " + suspectDB.size() + " suspects.");
    }

    /**
     * Builds an ArrayList from the suspect Map and sorts it using the
     * custom merge sort implementation below.
    *
     * @return ArrayList of all suspects ordered highest to lowest risk
     */
    public List<Suspect> sortSuspectsByRisk() {
        List<Suspect> list = new ArrayList<>(suspectDB.values());
        mergeSort(list, 0, list.size() - 1);
        return list;
    }

    /**
     * Recursive merge sort entry point.
     * Splits the list in half, sorts each half, then merges them.
     *
     * @param list  the list to sort
     * @param left  left boundary index (inclusive)
     * @param right right boundary index (inclusive)
     */
    private void mergeSort(List<Suspect> list, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        mergeSort(list, left, mid);
        mergeSort(list, mid + 1, right);
        merge(list, left, mid, right);
    }

    /**
     * Merges two sorted sublists back into the original list in descending order.
     * Uses Suspect.compareTo() for comparison, which orders by riskScore descending.
     *
     * @param list  the list containing both sublists
     * @param left  start of left sublist
     * @param mid   end of left sublist / start of right sublist
     * @param right end of right sublist
     */
    private void merge(List<Suspect> list, int left, int mid, int right) {
        List<Suspect> temp  = new ArrayList<>(list.subList(left, right + 1));
        int i     = 0;
        int j     = mid - left + 1;
        int k     = left;
        int size1 = mid - left + 1;
        int size2 = right - mid;

        while (i < size1 && j < size1 + size2) {
            // Descending order: highest risk first
            // compareTo returns negative when "this" has higher risk
            if (temp.get(i).compareTo(temp.get(j)) <= 0) {
                list.set(k, temp.get(i));
                i++;
                k++;
            } else {
                list.set(k, temp.get(j));
                j++;
                k++;
            }
        }
        while (i < size1) {
            list.set(k, temp.get(i));
            i++;
            k++;
        }
        while (j < size1 + size2) {
            list.set(k, temp.get(j));
            j++;
            k++;
        }
    }

    /**
     * Sorts all suspects by risk and prints a ranked table to the console.
     */
    public void viewSortedSuspectList() {
        List<Suspect> sorted = sortSuspectsByRisk();
        System.out.println("═".repeat(70));
        System.out.println("  SUSPECTS RANKED BY RISK (highest to lowest)");
        System.out.println("═".repeat(70));
        System.out.printf("  %-6s %-22s %-8s %-8s %-6s%n",
                "ID", "Name", "Score", "Level", "Flags");
        System.out.println("─".repeat(70));

        for (int i = 0; i < sorted.size(); i++) {
            Suspect fraudster = sorted.get(i);

            // Count flagged transactions using a for loop
            int flags = 0;
            List<Transaction> transactions = fraudster.getTransactions();
            for (int j = 0; j < transactions.size(); j++) {
                Transaction fraud = transactions.get(j);
                if (fraud.isFlagged()) {
                    flags++;
                }
            }

            System.out.printf("  %-6d %-22s %-8.1f %-8s %-6d%n",
                    fraudster.getSuspectID(),
                    fraudster.getName(),
                    fraudster.getRiskScore(),
                    fraudster.getRiskLevel(),
                    flags);
        }
        System.out.println("═".repeat(70));
    }
}