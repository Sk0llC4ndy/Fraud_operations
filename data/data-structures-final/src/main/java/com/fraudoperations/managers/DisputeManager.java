package com.fraudoperations.managers;

import com.fraudoperations.models.*;

import java.util.*;

/**
 * Feature 6 - Dispute and Abuse Tracking
 *
 * Opens, tracks, and resolves disputes linked to suspects and transactions.
 * Disputes are stored in a LinkedHashMap for O(1) lookup and also linked
 * to each Suspect's dispute ArrayList.
 *
 * Data structures used:
 *   LinkedHashMap<Integer, Dispute> - dispute lookup table (Map)
 *   Map<Integer, Suspect>           - reference to master suspect database
 */
public class DisputeManager {

    /** Reference to the master suspect database. */
    private final Map<Integer, Suspect> suspectDB;

    /** Dispute lookup table: disputeID to Dispute. Backed by LinkedHashMap. */
    private final Map<Integer, Dispute> disputeDB = new LinkedHashMap<>();

    /** Auto-incrementing dispute ID starting above transaction ID range. */
    private int nextDisputeID = 7000;

    /**
     * Constructs a DisputeManager backed by the shared suspect database.
     *
     * @param suspectDB the master suspect Map
     */
    public DisputeManager(Map<Integer, Suspect> suspectDB) {
        this.suspectDB = suspectDB;
    }

    /**
     * Opens a new dispute, stores it in the dispute Map,
     * and appends it to the suspect's dispute ArrayList.
     *
     * @param suspectID     ID of the suspect associated with the dispute
     * @param transactionID ID of the disputed transaction
     * @param dateOpened    date the dispute was opened (YYYY-MM-DD)
     * @return the newly created Dispute
     */
    public Dispute addDispute(int suspectID, int transactionID, String dateOpened) {
        Dispute dsp = new Dispute(nextDisputeID++, transactionID, suspectID, dateOpened);
        disputeDB.put(dsp.getDisputeID(), dsp);
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster != null) {
            fraudster.addDispute(dsp);
        }
        System.out.println("Opened: " + dsp);
        return dsp;
    }

    /**
     * Resolves a dispute by setting its status to RESOLVED,
     * recording the outcome, and flagging whether it was a financial loss.
     *
     * @param disputeID  ID of the dispute to resolve
     * @param resolution one of: LOSS, NOT_AT_FAULT, FRAUD_CONFIRMED
     * @param dateClosed date the dispute was closed (YYYY-MM-DD)
     * @return true if resolved successfully; false if not found
     */
    public boolean resolveDispute(int disputeID, String resolution, String dateClosed) {
        Dispute dsp = disputeDB.get(disputeID);
        if (dsp == null) {
            System.out.println("Dispute not found.");
            return false;
        }
        dsp.setResolution(resolution);
        dsp.setDateClosed(dateClosed);
        dsp.setStatus("RESOLVED");
        dsp.setResolved(true);
        dsp.setWasLoss(resolution.equalsIgnoreCase("LOSS"));
        System.out.println("Resolved dispute " + disputeID + " -> " + resolution);
        return true;
    }

    /**
     * Moves a dispute through its lifecycle status stages.
     *
     * @param disputeID the ID of the dispute to update
     * @param newStatus the new status: OPEN, UNDER_REVIEW, or RESOLVED
     */
    public void updateDisputeStatus(int disputeID, String newStatus) {
        Dispute dsp = disputeDB.get(disputeID);
        if (dsp == null) {
            System.out.println("Dispute not found.");
            return;
        }
        dsp.setStatus(newStatus);
        System.out.println("Dispute " + disputeID + " status -> " + newStatus);
    }

    /**
     * Returns all disputes linked to a given suspect from their ArrayList.
     * Used when the CSV dataset has a long history of transactions old and new.
     *
     * @param suspectID the suspect to look up
     * @return the dispute ArrayList, or an empty list if not found
     */
    public List<Dispute> getDisputeHistory(int suspectID) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) {
            return Collections.emptyList();
        } else {
            return fraudster.getDisputes();
        }
    }

    /**
     * Counts unresolved disputes for a suspect.
     *
     * @param suspectID the suspect to check
     * @return count of open disputes
     */
    public int countOpenDisputes(int suspectID) {
        int count = 0;
        for (Dispute dsp : getDisputeHistory(suspectID)) {
            if (!dsp.isResolved()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts resolved disputes for a suspect.
     *
     * @param suspectID the suspect to check
     * @return count of resolved disputes
     */
    public int countResolvedDisputes(int suspectID) {
        int count = 0;
        for (Dispute dsp : getDisputeHistory(suspectID)) {
            if (dsp.isResolved()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Prints a breakdown of dispute outcomes for a suspect:
     * losses, not-at-fault, and fraud-confirmed counts.
     *
     * @param suspectID the suspect to summarize
     */
    public void getDisputeOutcomeSummary(int suspectID) {
        List<Dispute> disputes = getDisputeHistory(suspectID);

        long losses     = 0;
        long notAtFault = 0;
        long fraudConf  = 0;

        for (Dispute dsp : disputes) {
            if (dsp.isWasLoss()) {
                losses++;
            }
            if ("NOT_AT_FAULT".equalsIgnoreCase(dsp.getResolution())) {
                notAtFault++;
            }
            if ("FRAUD_CONFIRMED".equalsIgnoreCase(dsp.getResolution())) {
                fraudConf++;
            }
        }

        System.out.printf("Suspect %d Dispute Summary - Losses: %d | Not At Fault: %d | Fraud Confirmed: %d%n",
                suspectID, losses, notAtFault, fraudConf);
    }
}