package com.fraudoperations.systems;

import com.fraudoperations.models.Suspect;

import java.util.*;

/**
 * Feature 5 - Hash Indexing System
 * Normalizes and hashes all four identifiers (IP, email, address, name)
 * for fast cross-suspect lookup and connection detection.
 *
 * Data structures used:
 *   HashMap<Integer, List<Integer>> - four index maps (IP, email, address, name)
 *   ArrayList<Integer>              - list of suspect IDs at each hash bucket
 */
public class HashIndexSystem {

    /** Prime number for better hash distribution across the table. */
    private static final int TABLE_SIZE = 9973;

    // Four index maps: hash key to list of suspectIDs at that key
    private final Map<Integer, List<Integer>> ipIndex      = new HashMap<>();
    private final Map<Integer, List<Integer>> emailIndex   = new HashMap<>();
    private final Map<Integer, List<Integer>> addressIndex = new HashMap<>();
    private final Map<Integer, List<Integer>> nameIndex    = new HashMap<>();

    // Normalize methods

    /**
     * Normalizes an IP address by stripping dots and trimming whitespace.
     *
     * @param ip raw IP address string
     * @return normalized IP string
     */
    public String normalizeIP(String ip) {
        if (ip == null) {
            return "";
        }
        return ip.replace(".", "").trim();
    }

    /**
     * Normalizes an email address by lowercasing and trimming whitespace.
     *
     * @param email raw email string
     * @return normalized email string
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.toLowerCase().trim();
    }

    /**
     * Normalizes a mailing address for consistent hashing.
     * Addresses can become confusing quickly depending on how they are inserted.
     * Abbreviations simplify inputs.
     * [^a-z0-9] catches anything not lowercase, number, or space — deletes commas, periods, dashes.
     * \\s+ catches multiple spaces and collapses them to a single space.
     *
     * @param address raw address string
     * @return normalized address string
     */
    public String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        return address.toLowerCase()
                .replace("street", "st")
                .replace("avenue", "ave")
                .replace("apartment", "apt")
                .replace("drive", "dr")
                .replace("boulevard", "blvd")
                .replace("road", "rd")
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Normalizes a person's name by lowercasing and removing common titles.
     *
     * @param name raw name string
     * @return normalized name string
     */
    public String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase()
                .replace("mr.", "")
                .replace("ms.", "")
                .replace("mrs.", "")
                .replace("dr.", "")
                .replace("jr.", "")
                .replace("sr.", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Hash methods using polynomial rolling hash

    /**
     * Applies a polynomial rolling hash to the input string.
     * Formula: hash = (hash * 31 + c) % TABLE_SIZE for each character c.
     *
     * @param s the normalized string to hash
     * @return non-negative integer hash key
     */
    private int polynomialHash(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        int hash  = 0;
        int prime = 31;
        for (char c : s.toCharArray()) {
            hash = (hash * prime + c) % TABLE_SIZE;
        }
        return Math.abs(hash);
    }

    /**
     * Hashes an IP address after normalizing it.
     *
     * @param ip the IP address to hash
     * @return hash key
     */
    public int hashIP(String ip) {
        return polynomialHash(normalizeIP(ip));
    }

    /**
     * Hashes an email address after normalizing it.
     *
     * @param email the email to hash
     * @return hash key
     */
    public int hashEmail(String email) {
        return polynomialHash(normalizeEmail(email));
    }

    /**
     * Hashes a mailing address after normalizing it.
     *
     * @param address the address to hash
     * @return hash key
     */
    public int hashAddress(String address) {
        return polynomialHash(normalizeAddress(address));
    }

    /**
     * Hashes a person's name after normalizing it.
     *
     * @param name the name to hash
     * @return hash key
     */
    public int hashName(String name) {
        return polynomialHash(normalizeName(name));
    }

    // Index methods

    /**
     * Registers a suspect into all four index maps in a single call.
     * Called by SuspectManager on addSuspect() and updateSuspect().
     *
     * @param fraudster the suspect to index
     */
    public void indexSuspect(Suspect fraudster) {
        int id = fraudster.getSuspectID();
        insertIntoIndex(ipIndex,      hashIP(fraudster.getIpAddress()),    id);
        insertIntoIndex(emailIndex,   hashEmail(fraudster.getEmail()),     id);
        insertIntoIndex(addressIndex, hashAddress(fraudster.getAddress()), id);
        insertIntoIndex(nameIndex,    hashName(fraudster.getName()),       id);
    }

    /**
     * Removes a suspect from all four index maps.
     * Called by SuspectManager on removeSuspect().
     *
     * @param fraudster the suspect to remove
     */
    public void removeSuspect(Suspect fraudster) {
        int id = fraudster.getSuspectID();
        removeFromIndex(ipIndex,      hashIP(fraudster.getIpAddress()),    id);
        removeFromIndex(emailIndex,   hashEmail(fraudster.getEmail()),     id);
        removeFromIndex(addressIndex, hashAddress(fraudster.getAddress()), id);
        removeFromIndex(nameIndex,    hashName(fraudster.getName()),       id);
    }

    /**
     * Inserts a suspect ID into the ArrayList at the given key.
     * Creates the ArrayList if no bucket exists yet.
     *
     * @param index     the index map to insert into
     * @param key       the hash key
     * @param suspectID the suspect ID to add
     */
    private void insertIntoIndex(Map<Integer, List<Integer>> index, int key, int suspectID) {
        List<Integer> list = index.get(key);
        if (list == null) {
            list = new ArrayList<>();
            index.put(key, list);
        }
        if (!list.contains(suspectID)) {
            list.add(suspectID);
        }
    }

    /**
     * Removes a suspect ID from the ArrayList at the given key.
     *
     * @param index     the index map to remove from
     * @param key       the hash key
     * @param suspectID the suspect ID to remove
     */
    private void removeFromIndex(Map<Integer, List<Integer>> index, int key, int suspectID) {
        List<Integer> list = index.get(key);
        if (list != null) {
            list.remove(Integer.valueOf(suspectID));
        }
    }

    // Lookup methods

    /**
     * Returns all suspect IDs that hash to the same bucket as the given IP.
     *
     * @param ip the IP address to look up
     * @return ArrayList of suspect IDs, or an empty list if none
     */
    public List<Integer> lookupByIP(String ip) {
        return lookup(ipIndex, hashIP(ip));
    }

    /**
     * Returns all suspect IDs that hash to the same bucket as the given email.
     *
     * @param email the email to look up
     * @return ArrayList of suspect IDs, or an empty list if none
     */
    public List<Integer> lookupByEmail(String email) {
        return lookup(emailIndex, hashEmail(email));
    }

    /**
     * Returns all suspect IDs that hash to the same bucket as the given address.
     *
     * @param address the address to look up
     * @return ArrayList of suspect IDs, or an empty list if none
     */
    public List<Integer> lookupByAddress(String address) {
        return lookup(addressIndex, hashAddress(address));
    }

    /**
     * Returns all suspect IDs that hash to the same bucket as the given name.
     *
     * @param name the name to look up
     * @return ArrayList of suspect IDs, or an empty list if none
     */
    public List<Integer> lookupByName(String name) {
        return lookup(nameIndex, hashName(name));
    }

    /**
     * Internal lookup: returns the list at the given key, or an empty list.
     *
     * @param index the index map to query
     * @param key   the hash key
     * @return the ArrayList at that key, or Collections.emptyList()
     */
    private List<Integer> lookup(Map<Integer, List<Integer>> index, int key) {
        return index.getOrDefault(key, Collections.emptyList());
    }

    // Shared identifier detection

    /**
     * Returns true if more than one suspect maps to the same IP hash key.
     *
     * @param ip the IP address to check
     * @return true if IP is shared
     */
    public boolean isSharedIP(String ip) {
        return lookupByIP(ip).size() > 1;
    }

    /**
     * Returns true if more than one suspect maps to the same email hash key.
     *
     * @param email the email to check
     * @return true if email is shared
     */
    public boolean isSharedEmail(String email) {
        return lookupByEmail(email).size() > 1;
    }

    /**
     * Returns true if more than one suspect maps to the same address hash key.
     *
     * @param address the address to check
     * @return true if address is shared
     */
    public boolean isSharedAddress(String address) {
        return lookupByAddress(address).size() > 1;
    }

    // Index accessors used by ConnectionGraph

    /** @return the IP index map */
    public Map<Integer, List<Integer>> getIpIndex() {
        return ipIndex;
    }

    /** @return the email index map */
    public Map<Integer, List<Integer>> getEmailIndex() {
        return emailIndex;
    }

    /** @return the address index map */
    public Map<Integer, List<Integer>> getAddressIndex() {
        return addressIndex;
    }

    /** @return the name index map */
    public Map<Integer, List<Integer>> getNameIndex() {
        return nameIndex;
    }
}