package com.fraudoperations.managers;

import com.fraudoperations.models.Suspect;
import com.fraudoperations.models.Transaction;

import java.io.*;
import java.util.*;

/**
 * Reads cardholder and transaction data from CSV files and loads them into memory.
 *
 * This is the first class called at startup. It populates the master suspect
 * database and flat transaction list, then links each transaction to its
 * owning Suspect object.
 *
 * cardholders.csv columns:
 *   suspectID, name, email, phone, address, city, state, zip,
 *   ipAddress, availableBalance, age, riskScore, riskLevel, notes
 *
 * transactions.csv columns:
 *   transactionID, suspectID, nameOnTransaction, emailOnTransaction,
 *   billingAddress, shippingAddress, amount, date, time, isOnline,
 *   ipAddress, isFlagged, flagReason, merchantCategory, merchantID,
 *   authAttempts, disputeFiled, disputeFiledBy, notes
 *
 * Data structures used:
 *   LinkedHashMap<Integer, Suspect> - preserves insertion order for suspect display
 *   ArrayList<Transaction>          - flat list of all loaded transactions
 *   ArrayList<String>               - internal use in custom CSV parser
 */
public class CSVLoader {

    /** Path to the cardholders CSV file. */
    private final String cardholderPath;

    /** Path to the transactions CSV file. */
    private final String transactionPath;

    /**
     * Constructs a CSVLoader with the specified file paths.
     *
     * @param cardholderPath  path to cardholders.csv
     * @param transactionPath path to transactions.csv
     */
    public CSVLoader(String cardholderPath, String transactionPath) {
        this.cardholderPath  = cardholderPath;
        this.transactionPath = transactionPath;
    }

    /**
     * Reads cardholders.csv and returns a LinkedHashMap of all suspects.
     * The header row is skipped automatically. Address fields (street, city,
     * state, ZIP) are concatenated into a single full-address string.
     *
     * @return LinkedHashMap mapping suspectID to Suspect object
     */
    public Map<Integer, Suspect> loadSuspects() {
        Map<Integer, Suspect> suspects = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cardholderPath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] col = parseLine(line);
                if (col.length < 10) {
                    continue;
                }

                int    suspectID        = Integer.parseInt(col[0].trim());
                String name             = col[1].trim();
                String email            = col[2].trim();
                String phone            = col[3].trim();
                String address          = col[4].trim();
                String city             = col[5].trim();
                String state            = col[6].trim();
                String zip              = col[7].trim();
                String ip               = col[8].trim();
                double availableBalance = Double.parseDouble(col[9].trim());
                int    age              = col.length > 10 ? Integer.parseInt(col[10].trim()) : 0;

                String fullAddress = address + ", " + city + ", " + state + " " + zip;

                Suspect fraudster = new Suspect(suspectID, name, email, phone,
                        fullAddress, ip, age, availableBalance);
                suspects.put(suspectID, fraudster);
            }

        } catch (IOException e) {
            System.err.println("ERROR reading cardholders CSV: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("ERROR parsing number in cardholders CSV: " + e.getMessage());
        }

        System.out.println("Loaded " + suspects.size() + " cardholders.");
        return suspects;
    }

    /**
     * Reads transactions.csv and returns an ArrayList of all transactions.
     * The header row is skipped automatically. Optional columns
     * (disputeFiled, disputeFiledBy, notes) default to empty/false if absent.
     *
     * @return ArrayList of all loaded transactions
     */
    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(transactionPath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] col = parseLine(line);
                if (col.length < 16) {
                    continue;
                }

                int     transactionID      = Integer.parseInt(col[0].trim());
                int     suspectID          = Integer.parseInt(col[1].trim());
                String  nameOnTransaction  = col[2].trim();
                String  emailOnTransaction = col[3].trim();
                String  billingAddress     = col[4].trim();
                String  shippingAddress    = col[5].trim();
                double  amount             = Double.parseDouble(col[6].trim());
                String  date               = col[7].trim();
                String  time               = col[8].trim();
                boolean isOnline           = col[9].trim().equalsIgnoreCase("true");
                String  ip                 = col[10].trim();
                boolean isFlagged          = col[11].trim().equalsIgnoreCase("true");
                String  flagReason         = col[12].trim();
                String  merchantCategory   = col[13].trim();
                String  merchantID         = col[14].trim();
                int     authAttempts       = Integer.parseInt(col[15].trim());
                boolean disputeFiled       = col.length > 16 && col[16].trim().equalsIgnoreCase("true");
                String  disputeFiledBy     = col.length > 17 ? col[17].trim() : "";
                String  notes              = col.length > 18 ? col[18].trim() : "";

                Transaction fraud = new Transaction(
                        transactionID, suspectID, nameOnTransaction,
                        emailOnTransaction, billingAddress, shippingAddress,
                        amount, date, time, isOnline, ip,
                        isFlagged, flagReason, merchantCategory,
                        merchantID, authAttempts, disputeFiled,
                        disputeFiledBy, notes);

                transactions.add(fraud);
            }

        } catch (IOException e) {
            System.err.println("ERROR reading transactions CSV: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("ERROR parsing number in transactions CSV: " + e.getMessage());
        }

        System.out.println("Loaded " + transactions.size() + " transactions.");
        return transactions;
    }

    /**
     * Iterates all loaded transactions and appends each one to its owning
     * Suspect's transaction ArrayList. Logs a warning for any transaction
     * whose suspectID does not match a loaded suspect.
     *
     * @param suspects     the loaded suspect Map from loadSuspects()
     * @param transactions the loaded transaction list from loadTransactions()
     */
    public void linkTransactionsToSuspects(Map<Integer, Suspect> suspects,
                                           List<Transaction> transactions) {
        int linked = 0;
        for (Transaction fraud : transactions) {
            Suspect fraudster = suspects.get(fraud.getSuspectID());
            if (fraudster != null) {
                fraudster.addTransaction(fraud);
                linked++;
            } else {
                System.out.println("WARNING: TXN-" + fraud.getTransactionID()
                        + " references unknown suspectID " + fraud.getSuspectID());
            }
        }
        // Print summary after the loop, not inside it
        System.out.println("Linked " + linked + " transactions to suspects.");
    }

    /**
     * Parses a single CSV line into a String array, correctly handling
     * fields that contain commas inside double quotes.
     *
     * @param line the raw CSV line to parse
     * @return array of field values with quotes stripped
     */
    private String[] parseLine(String line) {
        List<String> result   = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes      = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}