package com.fraudoperations.managers;

import com.fraudoperations.models.Suspect;
import com.fraudoperations.models.Transaction;

import java.util.*;

/**
 * Features 2, 3, 4 - Transaction Management + Online/In-Person Checker
 * Logs, categorizes, and flags transactions. Runs mismatch validation.
 *
 * Data structures used:
 *   Queue<Transaction> backed by LinkedList - flagged transactions pending review
 *   Map<Integer, Suspect>                  - reference to the master suspect database
 */
public class TransactionManager {

    /** Reference to the master suspect database for linking transactions. */
    private final Map<Integer, Suspect> suspectDB;

    /**
     * Queue of flagged transactions pending investigator review.
     * Implemented as a LinkedList to satisfy the Queue/LinkedList requirement.
     */
    private final Queue<Transaction> reviewQueue = new LinkedList<>();

    /** Auto-incrementing ID for manually added transactions. */
    private int nextTxnID = 5000;

    // Mismatch flags set during the current transaction evaluation cycle
    private boolean nameMismatch;
    private boolean emailMismatch;
    private boolean addressMismatch;

    /**
     * Constructs a TransactionManager backed by the shared suspect database.
     *
     * @param suspectDB the master suspect Map from CSVLoader
     */
    public TransactionManager(Map<Integer, Suspect> suspectDB) {
        this.suspectDB = suspectDB;
    }

    /**
     * Creates a new Transaction, links it to the owning suspect's ArrayList,
     * and routes it through validation checks.
     *
     * @param suspectID       ID of the cardholder
     * @param name            name on the transaction
     * @param email           email used
     * @param billing         billing address
     * @param shipping        shipping address
     * @param amount          dollar amount
     * @param date            date (YYYY-MM-DD)
     * @param time            time (HH:MM)
     * @param isOnline        true if card-not-present
     * @param ip              IP address (online only)
     * @param merchantCategory merchant category
     * @param merchantID      merchant ID
     * @param authAttempts    number of auth attempts
     * @param notes           investigator notes
     * @return the created Transaction object
     */
    public Transaction addTransaction(int suspectID, String name, String email,
                                      String billing, String shipping,
                                      double amount, String date, String time,
                                      boolean isOnline, String ip,
                                      String merchantCategory, String merchantID,
                                      int authAttempts, String notes) {
        Transaction fraud = new Transaction(
                nextTxnID++, suspectID, name, email,
                billing, shipping, amount, date, time,
                isOnline, ip, false, "",
                merchantCategory, merchantID,
                authAttempts, false, "", notes);

        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster != null) {
            fraudster.addTransaction(fraud);
        }
        categorizeTransaction(fraud);
        return fraud;
    }

    /**
     * Routes a transaction to online or in-person validation based on channel.
     * Flagged transactions are enqueued into the LinkedList-backed review queue.
     *
     * @param fraud the transaction to validate
     */
    public void categorizeTransaction(Transaction fraud) {
        if (fraud.isOnline()) {
            runOnlineChecks(fraud);
        } else {
            runInPersonChecks(fraud);
        }
        if (fraud.isFlagged()) {
            reviewQueue.add(fraud);
        }
    }

    /**
     * Sets isFlagged to true on a transaction and appends the reason code
     * to the pipe-delimited flagReason string.
     *
     * @param fraud  the transaction to flag
     * @param reason the flag code to append (e.g. NAME_MISMATCH)
     */
    public void flagTransaction(Transaction fraud, String reason) {
        fraud.setFlagged(true);
        String existing = fraud.getFlagReason();
        String updatedReason;
        if (existing == null || existing.isBlank()) {
            updatedReason = reason;
        } else {
            updatedReason = existing + "|" + reason;
        }
        fraud.setFlagReason(updatedReason);
    }

    /**
     * Returns all transactions linked to the specified suspect.
     *
     * @param suspectID the suspect to look up
     * @return the ArrayList of transactions, or an empty list if not found
     */
    public List<Transaction> getTransactionHistory(int suspectID) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) {
            return Collections.emptyList();
        }
        return fraudster.getTransactions();
    }

    /**
     * Removes a specific transaction from a suspect's transaction ArrayList.
     * Iterates the list and breaks on first match to avoid ConcurrentModificationException.
     *
     * @param suspectID     the owning suspect ID
     * @param transactionID the transaction to remove
     * @return true if found and removed
     */
    public boolean removeTransaction(int suspectID, int transactionID) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) {
            return false;
        }
        // Check the fraudster (suspect) for their ID,
        // check fraud (transaction) for its ID,
        // remove transaction if both match
        boolean removed = false;
        List<Transaction> list = fraudster.getTransactions();
        for (int i = 0; i < list.size(); i++) {
            Transaction fraud = list.get(i);
            if (fraud.getTransactionID() == transactionID) {
                list.remove(i);
                removed = true;
                break;
            }
        }
        return removed;
    }

    /**
     * Runs online-specific fraud checks on a transaction.
     * For online transactions must:
     *   - Check name, email, and address for mismatch
     *   - Check auth attempts, amount thresholds,
     *     odd hours, CNP limit, and round amounts
     *
     * @param fraud the online transaction to check
     */
    private void runOnlineChecks(Transaction fraud) {
        Suspect fraudster = suspectDB.get(fraud.getSuspectID());
        if (fraudster == null) {
            return;
        }

        nameMismatch    = compareNameToRecord(fraud, fraudster);
        emailMismatch   = compareEmailToRecord(fraud, fraudster);
        addressMismatch = compareAddressToRecord(fraud, fraudster);

        // If mismatch is true, flag it
        if (nameMismatch) {
            flagTransaction(fraud, "NAME_MISMATCH");
        }
        if (emailMismatch) {
            flagTransaction(fraud, "EMAIL_MISMATCH");
        }
        if (addressMismatch) {
            flagTransaction(fraud, "ADDRESS_MISMATCH");
        }

        // Check amount, authorization attempts, hours, round amount, and address mismatch
        if (fraud.getAmount() > 5000) {
            flagTransaction(fraud, "AMOUNT_HIGH");
        }
        // CNP = card-not-present / online purchase exceeding the $1500 limit
        if (fraud.getAmount() > 1500) {
            flagTransaction(fraud, "CNP_HIGH_AMOUNT");
        }
        if (fraud.getAuthAttempts() > 2) {
            flagTransaction(fraud, "AUTH_STUFFING");
        }
        if (isOddHour(fraud.getTime())) {
            flagTransaction(fraud, "ODD_HOURS");
        }
        if (isRoundAmount(fraud.getAmount())) {
            flagTransaction(fraud, "ROUND_AMOUNT");
        }
        if (!fraud.getBillingAddress().equals(fraud.getShippingAddress())) {
            flagTransaction(fraud, "ADDRESS_MISMATCH");
        }
    }

    /**
     * Runs in-person fraud checks: name, email, address mismatch,
     * and high-amount threshold.
     *
     * @param fraud the in-person transaction to check
     */
    private void runInPersonChecks(Transaction fraud) {
        Suspect fraudster = suspectDB.get(fraud.getSuspectID());
        if (fraudster == null) {
            return;
        }
        if (checkInPersonName(fraud, fraudster)) {
            flagTransaction(fraud, "NAME_MISMATCH");
        }
        if (checkInPersonEmail(fraud, fraudster)) {
            flagTransaction(fraud, "EMAIL_MISMATCH");
        }
        if (checkInPersonAddress(fraud, fraudster)) {
            flagTransaction(fraud, "ADDRESS_MISMATCH");
        }
        if (fraud.getAmount() > 5000) {
            flagTransaction(fraud, "AMOUNT_HIGH");
        }
    }

    // In-Person comparison helpers
    private boolean checkInPersonName(Transaction fraud, Suspect fraudster) {
        return !fraud.getNameOnTransaction().equalsIgnoreCase(fraudster.getName());
    }

    private boolean checkInPersonEmail(Transaction fraud, Suspect fraudster) {
        return !fraud.getEmailOnTransaction().equalsIgnoreCase(fraudster.getEmail());
    }

    private boolean checkInPersonAddress(Transaction fraud, Suspect fraudster) {
        return !fraud.getBillingAddress().equalsIgnoreCase(fraudster.getAddress());
    }

    // Online comparison helpers
    private boolean compareNameToRecord(Transaction fraud, Suspect fraudster) {
        return !fraud.getNameOnTransaction().equalsIgnoreCase(fraudster.getName());
    }

    private boolean compareEmailToRecord(Transaction fraud, Suspect fraudster) {
        return !fraud.getEmailOnTransaction().equalsIgnoreCase(fraudster.getEmail());
    }

    private boolean compareAddressToRecord(Transaction fraud, Suspect fraudster) {
        return !fraud.getBillingAddress().equalsIgnoreCase(fraudster.getAddress());
    }

    /** Returns true if the transaction time is between midnight and 5 AM. */
    private boolean isOddHour(String time) {
        try {
            int hour = Integer.parseInt(time.split(":")[0]);
            return hour >= 0 && hour < 5;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns true if the amount is a positive multiple of $500. */
    private boolean isRoundAmount(double amount) {
        return amount > 0 && amount % 500 == 0;
    }

    /**
     * Generates a formatted report of all in-person validation results.
     *
     * @param fraud     the transaction
     * @param fraudster the owning suspect
     * @return formatted report string
     */
    public String generateInPersonReport(Transaction fraud, Suspect fraudster) {
        return String.format(
                "IN-PERSON REPORT | TXN-%d | Suspect %d (%s)%n" +
                "  Name Mismatch    : %s%n" +
                "  Email Mismatch   : %s%n" +
                "  Address Mismatch : %s%n" +
                "  Flagged          : %s%n" +
                "  Flag Reasons     : %s",
                fraud.getTransactionID(), fraudster.getSuspectID(), fraudster.getName(),
                checkInPersonName(fraud, fraudster),
                checkInPersonEmail(fraud, fraudster),
                checkInPersonAddress(fraud, fraudster),
                fraud.isFlagged(),
                fraud.getFlagReason());
    }

    /**
     * Generates a formatted report of all online validation results.
     *
     * @param fraud     the transaction
     * @param fraudster the owning suspect
     * @return formatted report string
     */
    public String generateOnlineReport(Transaction fraud, Suspect fraudster) {
        return String.format(
                "ONLINE REPORT | TXN-%d | Suspect %d (%s)%n" +
                "  IP Address       : %s%n" +
                "  Name Mismatch    : %s%n" +
                "  Email Mismatch   : %s%n" +
                "  Address Mismatch : %s%n" +
                "  Auth Attempts    : %d%n" +
                "  Amount           : $%.2f%n" +
                "  Flagged          : %s%n" +
                "  Flag Reasons     : %s",
                fraud.getTransactionID(), fraudster.getSuspectID(), fraudster.getName(),
                fraud.getIpAddress(),
                compareNameToRecord(fraud, fraudster),
                compareEmailToRecord(fraud, fraudster),
                compareAddressToRecord(fraud, fraudster),
                fraud.getAuthAttempts(),
                fraud.getAmount(),
                fraud.isFlagged(),
                fraud.getFlagReason());
    }

    /**
     * Returns the LinkedList-backed review queue of flagged transactions.
     *
     * @return the Queue of pending reviews
     */
    public Queue<Transaction> getReviewQueue() {
        return reviewQueue;
    }
}