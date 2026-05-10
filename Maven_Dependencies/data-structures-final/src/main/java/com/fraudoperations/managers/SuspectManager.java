package com.fraudoperations.managers;

import com.fraudoperations.models.*;
import com.fraudoperations.systems.HashIndexSystem;

import java.util.*;

/**
 * Feature 1 - Suspect management
 * Add, view, update, search, and remove suspect profiles.
 */

public class SuspectManager {
    
    /*Master suspect database: suspectID -> Suspect
    * The id is the correlator between the suspect creating a connection for them
    */
    private final Map<Integer, Suspect> suspectDB;
    private final HashIndexSystem HashIndex;
    private int nextID = 9000; // auto-increment for manually added suspects

    public SuspectManager(Map<Integer, Suspect> suspectDb, HashIndexSystem hashIndex) {
        this.suspectDB = suspectDB;
        this.hashIndex = hashIndex;
    }

    // Adding Suspect
    public Suspect addSuspect(String name, String email, String phone,
        String address, String ipAddress,
        int age, double availableBalance) {
        int id = nextID++;
        Suspect fraudster = new Suspect(id, name, email, phone, address, ipAddress,
        age, availableBalance);
        suspectDB.put(id, fraudster);
        hashIndex.indexSuspect(fraudster);
        System.out.println("Added suspect: " + fraudster);
        return fraudster;
    }

    // search for suspect
       public List<Suspect> searchSuspect(String query) {
        List<Suspect> results = new ArrayList<>();
        String q = query.toLowerCase().trim();
 
        for (Suspect fraudster: suspectDB.values()) {
            if (String.valueOf(fraudster.getSuspectID()).equals(q)
                    || fraudster.getName().toLowerCase().contains(q)
                    || fraudster.getEmail().toLowerCase().contains(q)) {
                results.add(fraudster);
            }
        }
        return results;
    }

    // update Suspect list
    public boolean updateSuspect(int suspectID, String field, String newValue) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) {
            System.out.println("Suspect " + suspectID + " not found.");
            return false;
        }
        switch (field.toLowerCase()) {
            case "name" -> fraudster.setName(newValue);
            case "email" -> fraudster.setEmail(newValue);
            case "phone" -> fraudster.setPhone(newValue);
            case "address" -> fraudster.setAddress(newValue);
            case "ip" -> fraudster.setIpAddress(newValue);
            default -> { 
            System.out.println("Unknown field: " + field);
            return false; 
        }
        }
        hashIndex.indexSuspect(fraudster); //re-index after update
        System.out.println("Updated " + field + " for suspect " + SuspectID);
        return true;
    }

    //view Suspect Profile
    public void viewSuspectProfile(int suspectID) {
        Suspect fraudster = suspectDB.get(suspectID);
        if (fraudster == null) { 
            System.out.println("Suspect not found."); 
            return; 
        }
 
        System.out.println("═".repeat(60));
        System.out.printf("  SUSPECT PROFILE — ID: %d%n", s.getSuspectID());
        System.out.println("═".repeat(60));
        // % usage will ensure appropriate formatting during output
        System.out.printf("  Name      : %s%n", fraudster.getName());
        System.out.printf("  Email     : %s%n", fraudster.getEmail());
        System.out.printf("  Phone     : %s%n", fraudster.getPhone());
        System.out.printf("  Address   : %s%n", fraudster.getAddress());
        System.out.printf("  IP        : %s%n", fraudster.getIpAddress());
        System.out.printf("  Age  : %d", fraudster.getAge());
        System.out.printf("  Balance   : $%.2f%n", fraudster.getAvailableBalance());
        System.out.printf("  Risk      : %.1f (%s)%n", fraudster.getRiskScore(), fraudster.getRiskLevel());
        System.out.println("─".repeat(60));
 
        System.out.println("  TRANSACTIONS (" + fraudster.getTransactions().size() + "):");
        for (Transaction t : fraudster.getTransactions()) System.out.println("    " + t);
 
        System.out.println("  DISPUTES (" + fraudster.getDisputes().size() + "):");
        for (Dispute d : fraudster.getDisputes()) System.out.println("    " + d);
 
        System.out.println("  CONNECTIONS: " + fraudster.getConnections());
        System.out.println("═".repeat(60));
    }

    //Link suspects together
    
    public void linkSuspects(int idA, int idB, String reason) {
        Suspect a = suspectDB.get(idA);
        Suspect b = suspectDB.get(idB);
        if (a == null || b == null) { 
            System.out.println("One or both suspects not found."); 
            return;
        }
        a.addConnection(idB);
        b.addConnection(idA);
        System.out.printf("Linked suspect %d ↔ %d | Reason: %s%n", idA, idB, reason);
    }

    // Remove Suspect
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

    // Getters
    
    public Suspect getSuspect(int suspectID) { 
        return suspectDB.get(suspectID); 
    }
    
    public Map<Integer, Suspect> getAllSuspects() { 
        return suspectDB; 
    }
 
    //Dump list of all suspects
    public void listAllSuspects() {
        if (suspectDB.isEmpty()) { 
            System.out.println("No suspects loaded."); 
            return; 
        }
        System.out.println("─".repeat(70));
        suspectDB.values().forEach(fraudster -> System.out.println("  " + fraudster));
        System.out.println("─".repeat(70));
    }
}
