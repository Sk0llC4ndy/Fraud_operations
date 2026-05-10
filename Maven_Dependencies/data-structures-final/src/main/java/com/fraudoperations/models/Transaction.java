package com.fraudoperations.models;

public class Transaction {
    //declaration
    private int    transactionID;
    private int    suspectID;
    private String nameOnTransaction;
    private String emailOnTransaction;
    private String billingAddress;
    private String shippingAddress;
    private double amount;
    private String date;
    private String time;
    private boolean isOnline;
    private String  ipAddress;       // populated for online transactions only
    private boolean isFlagged;
    private String  flagReason;
    private String  merchantCategory;
    private String  merchantID;
    private int     authAttempts;
    private String  notes;

    //Constructors
    public Transaction(int transactionID, int suspectID, String nameOnTransactions, String emailOnTransaction, String billingAddress, 
    String shippingAddress, double amount, String date, String time, boolean isOnline, String ipAddress, boolean isFlagged, String flagReason,
    String merchantCategory, String merchantID,int authAttempts, 
    boolean disputeFiled, String disputeFiledBy, String notes) {
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

    //Getterrs / Setters
    public int getTransactionID() { 
        return transactionID; 
    }
    public void setTransactionID(int transactionID)  { 
        this.transactionID = transactionID; 
    }
 
    public int getSuspectID() { 
        return suspectID; 
    }
    public void setSuspectID(int suspectID) { 
        this.suspectID = suspectID; 
    }
 
    public String getNameOnTransaction() { 
        return nameOnTransaction; 
    }
    public void setNameOnTransaction(String n) { 
        this.nameOnTransaction = n; 
    }
 
    public String getEmailOnTransaction() { 
        return emailOnTransaction;
    }
    public void setEmailOnTransaction(String e) { 
        this.emailOnTransaction = e; 
    }
 
    public String getBillingAddress() { 
        return billingAddress; 
    }
    public void setBillingAddress(String billing) { 
        this.billingAddress = billing; 
    }
 
    public String getShippingAddress() { 
        return shippingAddress; 
    }
    public void setShippingAddress(String shipping) { 
        this.shippingAddress = shipping; 
    }
 
    public double getAmount()  { 
        return amount; 
    }
    public void setAmount(double amount)  { 
        this.amount = amount; 
    }
 
    public String getDate() { 
        return date;
    }
    public void setDate(String date) { 
        this.date = date; 
    }
 
    public String getTime() { 
        return time; 
    }
    public void setTime(String time)  { 
        this.time = time; 
    }
 
    public boolean isOnline() { 
        return isOnline; 
    }
    public void setOnline(boolean online) { 
        this.isOnline = online; 
    }
 
    public String getIpAddress() { 
        return ipAddress; 
    }
    public void setIpAddress(String ip) { 
        this.ipAddress = ip; 
    }
 
    public boolean isFlagged() { 
        return isFlagged; 
    }
    public void setFlagged(boolean flagged) { 
        this.isFlagged = flagged; 
    }
 
    public String getFlagReason() { 
        return flagReason; 
    }
    public void setFlagReason(String reason) { 
        this.flagReason = reason; 
    }
 
    public String getMerchantCategory() { 
        return merchantCategory; 
    }
    public void setMerchantCategory(String mc) { 
        this.merchantCategory = mc; 
    }
 
    public String getMerchantID() { 
        return merchantID; 
    }
    public void setMerchantID(String merchantid) { 
        this.merchantID = merchantid; 
    }
 
    public int getAuthAttempts() { 
        return authAttempts; 
    }
    public void setAuthAttempts(int attempts) { 
        this.authAttempts = attempts; 
    }

    public boolean isDisputeFiled() {
        return disputeFiled;
    }
    public void setDisputeFiled(boolean dspFiled) {
        this.disputeFiled = dspFiled;
    }

    public String getDisputeFiledby() {
        return disputeFiledBy; 
    }
    public void setDisputeFiledBy(String name) {
        this.disputeFiledBy = name;
    }
 
    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }

    //True if dispute was filed by someone other than the name on the transaction 
    public boolean isThirdPartyDispute() {
        if (!disputeFiled || disputeFiledBy == null || disputeFiledBy.isBlank()) {
            return false;
        }
        return !disputeFiledBy.equalsignoreCase(nameOnTransaction);
    }

    @Override
    public String toString() {
        return String.format ("TXN-%d | Suspect %d | $%.2f | %s %s | Online: %s | Flagged: %s%s | [%s]",
        transactionID, suspectID, amount, date, time, isOnline, isFlagged,
        disputeFiled ? " | DISPUTE by: " + disputeFiledBy : "",
        flagReason == null ? "" : flagReason);
    }
}
