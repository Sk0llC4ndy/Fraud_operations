package com.fraudoperations.models;

/**
 * Represents a single financial transaction linked to a Suspect.
 *
 * Each transaction records who made it, how it was made (online vs in-person),
 * whether it was flagged, which fraud rules triggered, and whether a dispute
 * was filed and by whom.
 *
 * The disputeFiledBy field is critical for elder abuse detection. When a third
 * party files a dispute on behalf of an elder cardholder, it triggers the
 * DISPUTE_ABUSE flag.
 */
public class Transaction {

    // Field declarations
    private int     transactionID;
    private int     suspectID;
    private String  nameOnTransaction;
    private String  emailOnTransaction;
    private String  billingAddress;
    private String  shippingAddress;
    private double  amount;
    private String  date;
    private String  time;
    private boolean isOnline;
    private String  ipAddress;        // populated for online transactions only
    private boolean isFlagged;
    private String  flagReason;
    private String  merchantCategory;
    private String  merchantID;
    private int     authAttempts;
    private boolean disputeFiled;
    private String  disputeFiledBy;
    private String  notes;

    /**
     * Full constructor used by CSVLoader.
     *
     * @param transactionID      unique transaction identifier
     * @param suspectID          ID of the owning suspect
     * @param nameOnTransaction  name used during transaction
     * @param emailOnTransaction email used during transaction
     * @param billingAddress     billing address provided
     * @param shippingAddress    shipping destination
     * @param amount             dollar amount
     * @param date               date (YYYY-MM-DD)
     * @param time               time (HH:MM)
     * @param isOnline           true if card-not-present / online
     * @param ipAddress          IP address (online only)
     * @param isFlagged          true if any fraud rule triggered
     * @param flagReason         pipe-delimited flag codes
     * @param merchantCategory   merchant category
     * @param merchantID         merchant identifier
     * @param authAttempts       number of auth attempts
     * @param disputeFiled       true if a dispute was opened
     * @param disputeFiledBy     name of who filed the dispute
     * @param notes              investigator notes
     */
    public Transaction(int transactionID, int suspectID, String nameOnTransaction,
                       String emailOnTransaction, String billingAddress,
                       String shippingAddress, double amount, String date,
                       String time, boolean isOnline, String ipAddress,
                       boolean isFlagged, String flagReason,
                       String merchantCategory, String merchantID,
                       int authAttempts, boolean disputeFiled,
                       String disputeFiledBy, String notes) {
        this.transactionID      = transactionID;
        this.suspectID          = suspectID;
        this.nameOnTransaction  = nameOnTransaction;
        this.emailOnTransaction = emailOnTransaction;
        this.billingAddress     = billingAddress;
        this.shippingAddress    = shippingAddress;
        this.amount             = amount;
        this.date               = date;
        this.time               = time;
        this.isOnline           = isOnline;
        this.ipAddress          = ipAddress;
        this.isFlagged          = isFlagged;
        this.flagReason         = flagReason;
        this.merchantCategory   = merchantCategory;
        this.merchantID         = merchantID;
        this.authAttempts       = authAttempts;
        this.disputeFiled       = disputeFiled;
        this.disputeFiledBy     = disputeFiledBy;
        this.notes              = notes;
    }

    // Getters and Setters

    /** @return the unique transaction ID */
    public int getTransactionID() {
        return transactionID;
    }
    /** @param transactionID the new transaction ID */
    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    /** @return the ID of the suspect who owns this transaction */
    public int getSuspectID() {
        return suspectID;
    }
    /** @param suspectID the suspect ID */
    public void setSuspectID(int suspectID) {
        this.suspectID = suspectID;
    }

    /** @return the name as it appeared on the transaction */
    public String getNameOnTransaction() {
        return nameOnTransaction;
    }
    /** @param n the updated name on transaction */
    public void setNameOnTransaction(String n) {
        this.nameOnTransaction = n;
    }

    /** @return the email used during the transaction */
    public String getEmailOnTransaction() {
        return emailOnTransaction;
    }
    /** @param e the updated email on transaction */
    public void setEmailOnTransaction(String e) {
        this.emailOnTransaction = e;
    }

    /** @return the billing address provided */
    public String getBillingAddress() {
        return billingAddress;
    }
    /** @param billing the updated billing address */
    public void setBillingAddress(String billing) {
        this.billingAddress = billing;
    }

    /** @return the shipping address for the order */
    public String getShippingAddress() {
        return shippingAddress;
    }
    /** @param shipping the updated shipping address */
    public void setShippingAddress(String shipping) {
        this.shippingAddress = shipping;
    }

    /** @return the transaction amount in dollars */
    public double getAmount() {
        return amount;
    }
    /** @param amount the transaction amount */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /** @return the transaction date (YYYY-MM-DD) */
    public String getDate() {
        return date;
    }
    /** @param date the transaction date */
    public void setDate(String date) {
        this.date = date;
    }

    /** @return the transaction time (HH:MM) */
    public String getTime() {
        return time;
    }
    /** @param time the transaction time */
    public void setTime(String time) {
        this.time = time;
    }

    /** @return true if this was an online / card-not-present transaction */
    public boolean isOnline() {
        return isOnline;
    }
    /** @param online true if online */
    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    /** @return the IP address used (online transactions only) */
    public String getIpAddress() {
        return ipAddress;
    }
    /** @param ip the IP address */
    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

    /** @return true if any fraud flag was triggered */
    public boolean isFlagged() {
        return isFlagged;
    }
    /** @param flagged the flagged state */
    public void setFlagged(boolean flagged) {
        this.isFlagged = flagged;
    }

    /** @return pipe-delimited string of triggered flag codes */
    public String getFlagReason() {
        return flagReason;
    }
    /** @param reason the updated flag reason string */
    public void setFlagReason(String reason) {
        this.flagReason = reason;
    }

    /** @return the merchant category code */
    public String getMerchantCategory() {
        return merchantCategory;
    }
    /** @param mc the merchant category */
    public void setMerchantCategory(String mc) {
        this.merchantCategory = mc;
    }

    /** @return the merchant identifier */
    public String getMerchantID() {
        return merchantID;
    }
    /** @param merchantid the merchant ID */
    public void setMerchantID(String merchantid) {
        this.merchantID = merchantid;
    }

    /** @return the number of authentication attempts */
    public int getAuthAttempts() {
        return authAttempts;
    }
    /** @param attempts the auth attempt count */
    public void setAuthAttempts(int attempts) {
        this.authAttempts = attempts;
    }

    /** @return true if a dispute was filed on this transaction */
    public boolean isDisputeFiled() {
        return disputeFiled;
    }
    /** @param dspFiled true if a dispute was filed */
    public void setDisputeFiled(boolean dspFiled) {
        this.disputeFiled = dspFiled;
    }

    /** @return the name of the person who filed the dispute */
    public String getDisputeFiledBy() {
        return disputeFiledBy;
    }
    /** @param name the name of the dispute filer */
    public void setDisputeFiledBy(String name) {
        this.disputeFiledBy = name;
    }

    /** @return free-text investigator notes */
    public String getNotes() {
        return notes;
    }
    /** @param notes updated notes */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Detects whether a dispute was filed by someone other than the name
     * on the transaction — a primary signal for elder financial abuse.
     *
     * Returns true when:
     *   1. A dispute has been filed (disputeFiled == true)
     *   2. disputeFiledBy is non-blank
     *   3. disputeFiledBy does not case-insensitively match nameOnTransaction
     *
     * @return true if the dispute filer is a third party
     */
    public boolean isThirdPartyDispute() {
        if (!disputeFiled || disputeFiledBy == null || disputeFiledBy.isBlank()) {
            return false;
        }
        return !disputeFiledBy.equalsIgnoreCase(nameOnTransaction);
    }

    /**
     * Returns a compact summary of this transaction for console display.
     *
     * @return formatted transaction string
     */
    @Override
    public String toString() {
        String disputeInfo = "";
        if (disputeFiled) {
            disputeInfo = " | DISPUTE by: " + disputeFiledBy;
        }
        String flags = "";
        if (flagReason != null) {
            flags = flagReason;
        }
        return String.format("TXN-%d | Suspect %d | $%.2f | %s %s | Online: %s | Flagged: %s%s | [%s]",
                transactionID, suspectID, amount, date, time,
                isOnline, isFlagged, disputeInfo, flags);
    }
}