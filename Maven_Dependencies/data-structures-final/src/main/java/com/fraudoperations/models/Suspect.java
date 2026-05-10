package com.fraudoperations.models;

import java.util.*;

public class Suspect implements Comparable<Suspect> {
    /* System must be able to register each suspects name, ID, email, phone number, address, IP, and age
    information on the available balance, riskscore, and risklevel must also be sustained in the program
     */
    private int suspectID;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String ipAddress;
    private int age;
    private double availableBalance;
    private double riskScore;
    private String riskLevel; //LOW, MEDIUM, HIGH

    private List<Transaction> transactions = new ArrayList<>();
    private List<Dispute> disputes = new ArrayList<>();
    private List<Integer> connections = new ArrayList<>(); //Links suspectIDs if applicable
    //uses arraylist to contain information of transactions, disputes, and connections

    // Constructors

    public Suspect(int suspectID, String name, String email, String phone, String address, String ipAddress, int Age, double availableBalance) {
        this.suspectID = suspectID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.ipAddress = ipAddress;
        this.age = age;
        this.availableBalance = availableBalance;
        this.riskScore = 0.0;
        this.riskLevel = "LOW";
    }

    // Elder protection
    public boolean isElder() {
        return age >= 65;
    }

    // Comparable: sort comparable by riskScore (descending - Highest to lowest)

    @Override
    public int compareTo(Suspect other) {
        return Double.compare(other.riskScore, this.riskScore);
    }

    // Getter / Setters
    public int getSuspectID() {
        return suspectID;
    }
    public void setSuspectID(int suspectID) {
        this.suspectID = suspectID;
    } 

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public double getAvailableBalance() { 
        return availableBalance; 
    }
    public void setAvailableBalance(double availableBalance) { 
        this.availableBalance = availableBalance; 
    }
 
    public double getRiskScore() { 
        return riskScore; 
    }
    public void setRiskScore(double riskScore) {  
        this.riskScore = riskScore; 
    }
 
    public String getRiskLevel() { 
        return riskLevel; 
    }
    public void setRiskLevel(String riskLevel) { 
        this.riskLevel = riskLevel; 
    }
 
    //create section for the list
    public List<Transaction> getTransactions() { 
        return transactions; 
    }
    public void addTransaction(Transaction t) { 
        transactions.add(t); 
    }

    public List<Dispute> getDisputes() { 
        return disputes; 
    }
    public void addDispute(Dispute d) { 
        disputes.add(d); 
    }

    public List<Integer> getConnections() { 
        return connections; 
    } 
    //create a connection based on suspectIDS
    public void addConnection(int suspectID) {
        if (!connections.contains(suspectID)) connections.add(suspectID);
    }        
    
    @Override
    public String toString() {
        return String.format("[%d] %s | Age: %d%s | Risk: %.1f (%s) | IP: %s", 
            suspectID, name, age, isElder() ? " [ELDER]" : "",
            riskScore, riskLevel, ipAddress);
    }
}
