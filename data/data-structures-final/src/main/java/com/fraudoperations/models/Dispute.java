package com.fraudoperations.models;

/**
 * Represents a financial dispute filed against a Transaction.
 *
 * A Dispute tracks its full lifecycle from opening through resolution.
 * The resolution field is especially important for risk scoring:
 * disputes resolved as LOSS increase a suspect's risk score via RiskEngine.
 *
 * Valid status values:     OPEN, UNDER_REVIEW, RESOLVED
 * Valid resolution values: LOSS, NOT_AT_FAULT, FRAUD_CONFIRMED
 */
public class Dispute {

    /** Unique dispute identifier. Auto-incremented by DisputeManager. */
    private int disputeID;

    /** ID of the transaction this dispute is filed against. */
    private int transactionID;

    /** ID of the suspect (cardholder) associated with this dispute. */
    private int suspectID;

    /** Date the dispute was opened (YYYY-MM-DD). */
    private String dateOpened;

    /** Date the dispute was closed (YYYY-MM-DD). Null while open. */
    private String dateClosed;

    /**
     * Current lifecycle status.
     * One of: OPEN, UNDER_REVIEW, RESOLVED.
     */
    private String status;

    /**
     * Outcome of the dispute once resolved.
     * One of: LOSS, NOT_AT_FAULT, FRAUD_CONFIRMED.
     */
    private String resolution;

    /** True once the dispute has been resolved. */
    private boolean isResolved;

    /** True if the resolution was LOSS. Used in risk scoring. */
    private boolean wasLoss;

    /**
     * Constructor used by DisputeManager.addDispute().
     * Initializes status to OPEN, isResolved to false, wasLoss to false.
     *
     * @param disputeID     unique dispute identifier
     * @param transactionID ID of the associated transaction
     * @param suspectID     ID of the associated suspect
     * @param dateOpened    date the dispute was opened (YYYY-MM-DD)
     */
    public Dispute(int disputeID, int transactionID, int suspectID, String dateOpened) {
        this.disputeID     = disputeID;
        this.transactionID = transactionID;
        this.suspectID     = suspectID;
        this.dateOpened    = dateOpened;
        this.status        = "OPEN";
        this.isResolved    = false;
        this.wasLoss       = false;
    }

    // Getters and Setters

    /** @return the unique dispute ID */
    public int getDisputeID() {
        return disputeID;
    }
    /** @param id the new dispute ID */
    public void setDisputeID(int id) {
        this.disputeID = id;
    }

    /** @return the ID of the associated transaction */
    public int getTransactionID() {
        return transactionID;
    }
    /** @param id the transaction ID */
    public void setTransactionID(int id) {
        this.transactionID = id;
    }

    /** @return the ID of the associated suspect */
    public int getSuspectID() {
        return suspectID;
    }
    /** @param suspectID the suspect ID */
    public void setSuspectID(int suspectID) {
        this.suspectID = suspectID;
    }

    /** @return the date the dispute was opened */
    public String getDateOpened() {
        return dateOpened;
    }
    /** @param dateOpened the opening date */
    public void setDateOpened(String dateOpened) {
        this.dateOpened = dateOpened;
    }

    /** @return the date the dispute was closed, or null if still open */
    public String getDateClosed() {
        return dateClosed;
    }
    /** @param dateClosed the closing date */
    public void setDateClosed(String dateClosed) {
        this.dateClosed = dateClosed;
    }

    /** @return the current status: OPEN, UNDER_REVIEW, or RESOLVED */
    public String getStatus() {
        return status;
    }
    /** @param status the new status */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return the resolution: LOSS, NOT_AT_FAULT, or FRAUD_CONFIRMED */
    public String getResolution() {
        return resolution;
    }
    /** @param resolution the resolution outcome */
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /** @return true if this dispute has been resolved */
    public boolean isResolved() {
        return isResolved;
    }
    /** @param resolved the resolved state */
    public void setResolved(boolean resolved) {
        this.isResolved = resolved;
    }

    /** @return true if the resolution was a financial loss */
    public boolean isWasLoss() {
        return wasLoss;
    }
    /** @param wasLoss true if the outcome was a loss */
    public void setWasLoss(boolean wasLoss) {
        this.wasLoss = wasLoss;
    }

    /**
     * Returns a formatted summary of this dispute for console display.
     *
     * @return formatted dispute string
     */
    @Override
    public String toString() {
        return String.format("Dispute-%d | TXN-%d | Suspect %d | Status: %s | Resolution: %s",
                disputeID, transactionID, suspectID, status,
                resolution == null ? "PENDING" : resolution);
    }
}