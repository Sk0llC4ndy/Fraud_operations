package com.fraudoperations.managers;

import com.fraudoperations.models.*;
import com.fraudoperations.systems.HashIndexSystem;

import java.util.*;

/**
 * Feature 1 - Suspect Management
 * Add, view, update, search, and remove suspect profiles.
 *
 * The master database is a Map keyed by suspectID. Every add or update
 * operation re-indexes the suspect through HashIndexSystem so all four
 * hash maps remain consistent.
 *
 * Data structures used:
 *   Map<Integer, Suspect>  - master suspect database
 *   ArrayList<Suspect>     - search results returned to caller
 */
public class SuspectManager {

    /**
     * Master suspect database: suspectID to Suspect.
     * The ID is the correlator between the suspect creating a connection for them.
     */
    private final Map<Integer, Suspect> suspectDB;

    /** Hash index system for fast identifier-based lookup and connection detection. */
    private final HashIndexSystem hashIndex;

    /** Auto-increment for manually added suspects — starts above CSV ID range. */
    private int nextID = 9000;

    /**
     * Constructs a SuspectManager with an existing database and hash index.
     *
     * @param suspectDB the loaded suspect Map from CSVLoader
     * @param hashIndex the shared hash index system
     */
    public SuspectManager(Map<Integer, Suspect> suspectDB, HashIndexSystem hashIndex) {
        this.suspectDB = suspectDB;
        this.hashIndex = hashIndex;
    }

    /**
     * Creates a new Suspect, stores it in the master Map,
     * and registers it in all four hash indexes.
     *
     * @param name             full name
     * @param email            email address
     * @param phone            phone number
     * @param address          full mailing address
     * @param ip               IP address
     * @param age              age in years
     * @param availableBalance account balance
     * @return the newly created Suspect
     */
    public Suspect addSuspect(String name, String email, String phone,
                              String address, String ip,
                              int age, double availableBalance) {
        int id = nextID++;
        Suspect fraudster = new Suspect(id, name, email, phone, address, ip,
                age, availableBalance);
        suspectDB.put(id, fraudster);
        hashIndex.indexSuspect(fraudster);
        System.out.println("Added suspect: " + fraudster);
        return fraudster;
    }

    /**
     * Searches the suspect Map by ID, name substring, or email substring.
     * The search is case-insensitive.
     *
     * @param query the search string (ID, partial name, or partial email)
     * @return ArrayList of matching Suspect objects; empty list if none found
     */
    public List<Suspect> searchSuspect(String query) {
        List<Suspect> results = new ArrayList<>();
        String q = query.toLowerCase().trim();

        for (Suspect fraudster : suspectDB.values()) {
            if (String.valueOf(fraudster.getSuspectID()).equals(q)
                    || fraudster.getName().toLowerCase().contains(q)
                    || fraudster.getEmail().toLowerCase().contains(q)) {
                results.add(fraudster);
            }
        }
        return results;
    }

    /**
     * Updates a single field on an existing suspect and re-indexes them.
     * Valid field names: name, email, phone, address, ip.
     *
     * @param suspectID the ID of the suspect to update
     * @param field     the field name to change
     * @param newValue  the new value to set
     * @return true if the update succeeded; false if not found or unknown field
     */
    public boolean updateSuspect(int suspectID, String field, String newValue) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) {
            System.out.println("Suspect " + suspectID + " not found.");
            return false;
        }
        switch (field.toLowerCase()) {
            case "name":
                fraudster.setName(newValue);
                break;
            case "email":
                fraudster.setEmail(newValue);
                break;
            case "phone":
                fraudster.setPhone(newValue);
                break;
            case "address":
                fraudster.setAddress(newValue);
                break;
            case "ip":
                fraudster.setIpAddress(newValue);
                break;
            default:
                System.out.println("Unknown field: " + field);
                return false;
        }
        // Re-index after update so hash maps stay consistent
        hashIndex.indexSuspect(fraudster);
        System.out.println("Updated " + field + " for suspect " + suspectID);
        return true;
    }

    /**
     * Prints the full profile of a suspect to the console, including
     * all transactions, disputes, connections, and risk score.
     *
     * @param suspectID the ID of the suspect to display
     */
    public void viewSuspectProfile(int suspectID) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) {
            System.out.println("Suspect not found.");
            return;
        }

        System.out.println("═".repeat(60));
        System.out.printf("  SUSPECT PROFILE - ID: %d%n", fraudster.getSuspectID());
        System.out.println("═".repeat(60));
        // % usage ensures appropriate formatting during output
        System.out.printf("  Name    : %s%n", fraudster.getName());
        System.out.printf("  Email   : %s%n", fraudster.getEmail());
        System.out.printf("  Phone   : %s%n", fraudster.getPhone());
        System.out.printf("  Address : %s%n", fraudster.getAddress());
        System.out.printf("  IP      : %s%n", fraudster.getIpAddress());
        System.out.printf("  Age     : %d%s%n", fraudster.getAge(), fraudster.isElder() ? " [ELDER]" : "");
        System.out.printf("  Balance : $%.2f%n", fraudster.getAvailableBalance());
        System.out.printf("  Risk    : %.1f (%s)%n", fraudster.getRiskScore(), fraudster.getRiskLevel());
        System.out.println("─".repeat(60));

        System.out.println("  TRANSACTIONS (" + fraudster.getTransactions().size() + "):");
        for (Transaction t : fraudster.getTransactions()) {
            System.out.println("    " + t);
        }

        System.out.println("  DISPUTES (" + fraudster.getDisputes().size() + "):");
        for (Dispute d : fraudster.getDisputes()) {
            System.out.println("    " + d);
        }

        System.out.println("  CONNECTIONS: " + fraudster.getConnections());
        System.out.println("═".repeat(60));
    }

    /**
     * Manually creates a bidirectional connection between two suspects
     * and logs the reason for the link.
     *
     * @param idA    first suspect ID
     * @param idB    second suspect ID
     * @param reason description of why they are linked
     */
    public void linkSuspects(int idA, int idB, String reason) {
        Suspect a = suspectDB.get(idA);
        Suspect b = suspectDB.get(idB);
        if (a == null || b == null) {
            System.out.println("One or both suspects not found.");
            return;
        }
        a.addConnection(idB);
        b.addConnection(idA);
        System.out.printf("Linked suspect %d to %d | Reason: %s%n", idA, idB, reason);
    }

    /**
     * Removes a suspect from the master Map and cleans all four hash indexes.
     *
     * @param suspectID the ID of the suspect to remove
     * @return true if found and removed; false if not found
     */
    public boolean removeSuspect(int suspectID) {
        Suspect fraudster = suspectDB.remove(suspectID);
        if (fraudster == null) {
            System.out.println("Suspect not found.");
            return false;
        }
        hashIndex.removeSuspect(fraudster);
        System.out.println("Removed suspect " + suspectID);
        return true;
    }

    /**
     * Retrieves a single suspect by ID.
     *
     * @param suspectID the ID to look up
     * @return the Suspect, or null if not found
     */
    public Suspect getSuspect(int suspectID) {
        return suspectDB.get(suspectID);
    }

    /**
     * Returns the full suspect Map.
     *
     * @return the master Map of Integer to Suspect
     */
    public Map<Integer, Suspect> getAllSuspects() {
        return suspectDB;
    }

    /**
     * Dumps a one-line summary of every suspect in the database.
     */
    public void listAllSuspects() {
        if (suspectDB.isEmpty()) {
            System.out.println("No suspects loaded.");
            return;
        }
        System.out.println("─".repeat(70));
        for (Suspect fraudster : suspectDB.values()) {
            System.out.println("  " + fraudster);
        }
        System.out.println("─".repeat(70));
    }
}