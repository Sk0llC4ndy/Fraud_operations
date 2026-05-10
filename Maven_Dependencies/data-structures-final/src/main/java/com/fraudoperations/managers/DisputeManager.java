package com.fraudoperations.managers;

import com.fraudoperations.models.*;

import java.util.*;

/**
 * Feature 6 - Dispute and Abuse Tracking
 */

public class DisputeManager {
    
    private final Map<Integer, Suspect> suspectDB;
    private final Map<Integer, Dispute> disputeDB = new LinkedHashMap<>();
    private int nextDisputeID = 7000;

    public DisptueManager(Map<Integer, Suspect> suspectDB) {
        this.suspectDb = suspectDB;
    }

    // Add Dispute
    public Dispute addDispute(int suspectID, int transactionID, String dateOpened) {
        Dispute dsp = new Dispute(nextDisputeID++, transactionID, suspectID, dateOpened);
        disputeDb.put(dsp.getDisputeID(), dsp);
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster != null) fraudster.addDispute(dsp);
        System.out.println("Opened: " + dsp);
        return dsp;
    }

    //Resolving Dispute
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

    //update Status on Dispute
    public void updateDisputeStatus(int disputeID, String newStatus) {
        dispute dsp = disputeDB.get(disputeID);
        if (dsp == null) {
            System.out.println("Dispute not found.");
            return; 
        }
        dsp.setStatus(newStatus);
        System.out.println("Dispute " + disputeID + " status -> " + newStatus);
    }

    //Get History of Disputes (used in the case dataset from csv has a long history of transactions old and new)
    public List<Dispute> getDisputeHistory(int suspectID) {
        suspect fraudster = suspectDB.get(suspectID);
        if (fraduster == null) {
            return Collections.emptyList();
        } else {
            return fraudster.getDisputes();
        }
    }

    // Count the number of Open and resolved disputes 

    public int countOpenDisputes(int suspectID) {
      int count = 0;
      for (Dispute dsp : getDisputeHistory(suspectID)) {
          if (!dsp.isResolved()) {
            count++;
            }
        }

        return count;
    }
    public int countResolvedDisputes(int suspectID) {
        int count = 0;
        for (Dispute dsp : getDisputeHistory(suspectID)) {
            if (dsp.isResolved()) {
                count++;
            }
        }
        return count;
    }

    public void getDisputeOutcomeSummary(int suspectID) {
        List<Dispute> disputes = getDisputeHistory(suspectID);

        long losses = 0;
        long notAtFault = 0;
        long fraudConf = 0;
        for (Dispute dsp : disputes) {
            if (dsp.isWasLoss()) {
                losses++;
            }
            if ("NOT_AT_FAULT".equalsIgnoreCase(dsp.getResolution())) {
                notAtFault++;
            }

            if("FRAUD_CONFIRMED".equalsIgnoreCase(dsp.getResolution())) {
                fraudConf++;
            }
        }

        System.out.printf("Suspect %d Dispute Summary: %d | Not At Fault: %d | Fraud Confirmed: %d%n", suspectID, losses, notAtFault, fraudConf);
    }
}
