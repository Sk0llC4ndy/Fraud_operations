package com.fraudoperations.models;

public class Dispute {
     
    private int disputeID;
    private int transactionID;
    private int suspectID;
    private String dateOpened;
    private String dateClosed;
    private String status;      // OPEN, UNDER_REVIEW, RESOLVED
    private String resolution;  // LOSS, NOT_AT_FAULT, FRAUD_CONFIRMED
    private boolean isResolved;
    private boolean wasLoss;

    // Constructor
    
    public Dispute (int disputeID, int transactionID, int suspectID, String dateOpened, String status, boolean isResolved, boolean wasLoss) {
        this.disputeID = disputeID;
        this.transactionID = transactionID;
        this.suspectID = suspectID;
        this.dateOpened = dateOpened;
        this.status = "OPEN";
        this.isResolved = false;
        this.wasLoss = false;
    }

    // Getters / Setters
    public int getDisputeID() { 
        return disputeID; 
    }
    public void setDisputeID(int id) { 
        this.disputeID = id; 
    }
 
    public int getTransactionID() { 
        return transactionID; 
    }
    public void setTransactionID(int id) { 
        this.transactionID = id; 
    }
 
    public int getSuspectID() { 
        return suspectID; 
    }
    public void setSuspectID(int suspectID) { 
        this.suspectID = suspectID; 
    }
 
    public String getDateOpened() { 
        return dateOpened; 
    }
    public void setDateOpened(String dateOpened) { 
        this.dateOpened = dateOpened; 
    }
 
    public String getDateClosed() { 
        return dateClosed; 
    }
    public void setDateClosed(String dateClosed) { 
        this.dateClosed = dateClosed; 
    }
 
    public String getStatus() { 
        return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }
 
    public String getResolution() { 
        return resolution;
    }
    public void setResolution(String resolution) { 
        this.resolution = resolution; 
    }
 
    public boolean isResolved() { 
        return isResolved; 
    }
    public void setResolved(boolean resolved) { 
        this.isResolved = resolved; 
    }
 
    public boolean isWasLoss() { 
        return wasLoss; 
    }
    public void setWasLoss(boolean wasLoss) { 
        this.wasLoss = wasLoss; 
    }
}
