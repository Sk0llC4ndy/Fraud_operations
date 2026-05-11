package com.fraudoperations;

import com.fraudoperations.managers.*;
import com.fraudoperations.models.*;
import com.fraudoperations.systems.*;

import java.io.IOException;
import java.util.*;

/**
 * Fraud Detection Program - Entry Point
 *
 * On startup:
 *   1. Loads cardholders and transactions from CSV files
 *   2. Loads fraud detection rules from fraud_detection_rules.csv
 *   3. Evaluates all rules against all transactions (including elder abuse rules)
 *   4. Indexes all suspects into the hash system
 *   5. Builds the connection graph
 *   6. Calculates risk scores
 *   7. Presents an interactive menu
 */
public class Main {

    // ANSI Terminal Colors
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN  = "\u001B[36m";
    public static final String RESET = "\u001B[0m";

    public static void main(String[] args) throws InterruptedException {

        // Startup Banner
        System.out.println(GREEN);
        System.out.println("███████╗██████╗  █████╗ ██╗   ██╗██████╗  ██████╗ ██████╗ ███████╗");
        System.out.println("██╔════╝██╔══██╗██╔══██╗██║   ██║██╔══██╗██╔═══██╗██╔══██╗██╔════╝");
        System.out.println("█████╗  ██████╔╝███████║██║   ██║██║  ██║██║   ██║██████╔╝███████╗");
        System.out.println("██╔══╝  ██╔══██╗██╔══██║██║   ██║██║  ██║██║   ██║██╔═══╝ ╚════██║");
        System.out.println("██║     ██║  ██║██║  ██║╚██████╔╝██████╔╝╚██████╔╝██║     ███████║");
        System.out.println("╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚═╝     ╚══════╝");
        System.out.println(RESET);
        Thread.sleep(500);

        System.out.println(CYAN + "════════════════════════════════════════════════════════════");
        System.out.println("        FRAUD DETECTION & INVESTIGATION SYSTEM");
        System.out.println("════════════════════════════════════════════════════════════" + RESET);

        System.out.println();
        System.out.println(" Loading suspect database...");
        Thread.sleep(500);
        System.out.println(" Loading transaction records...");
        Thread.sleep(500);
        System.out.println(" Loading fraud detection rules...");
        Thread.sleep(500);
        System.out.println(" Evaluating rules against all transactions...");
        Thread.sleep(500);
        System.out.println(" Building fraud connection graph...");
        Thread.sleep(500);
        System.out.println(" Calculating risk scores...");
        Thread.sleep(500);
        System.out.println(" Initializing investigation systems...");
        Thread.sleep(500);
        System.out.println();

        // 1. Load CSV paths from config.properties
        Properties config = new Properties();
        try {
            config.load(Main.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            System.err.println("ERROR loading config.properties: " + e.getMessage());
            System.err.println("Falling back to default data/ paths.");
        } catch (NullPointerException e) {
            System.err.println("config.properties not found on classpath. Using defaults.");
        }

        String cardholderPath  = config.getProperty("cardholder.csv.path", "data\\data-structures-final\\data\\cardholders.csv");
        String transactionPath = config.getProperty("transaction.csv.path", "data\\data-structures-final\\data\\transactions.csv");
        String rulesPath       = config.getProperty("rules.csv.path",       "data\\data-structures-final\\data\\fraud_detection_rules.csv");
        
        // 2. Load cardholders and transactions
        CSVLoader loader = new CSVLoader(cardholderPath, transactionPath);
        Map<Integer, Suspect> suspectDB    = loader.loadSuspects();
        List<Transaction>     transactions = loader.loadTransactions();
        loader.linkTransactionsToSuspects(suspectDB, transactions);

        // 3. Load and evaluate fraud detection rules from CSV
        FraudRuleEngine ruleEngine = new FraudRuleEngine(rulesPath);
        ruleEngine.loadRules();
        ruleEngine.evaluateAll(suspectDB);

        // 4. Build hash index
        HashIndexSystem hashIndex = new HashIndexSystem();
        for (Suspect fraudster : suspectDB.values()) {
            hashIndex.indexSuspect(fraudster);
        }

        // 5. Build remaining systems
        SuspectManager     suspectMgr = new SuspectManager(suspectDB, hashIndex);
        TransactionManager txnMgr     = new TransactionManager(suspectDB);
        DisputeManager     disputeMgr = new DisputeManager(suspectDB);
        ConnectionGraph    graph      = new ConnectionGraph(hashIndex, suspectDB);
        RiskEngine         riskEngine = new RiskEngine(suspectDB, hashIndex);
        CaseReviewQueue    caseQueue  = new CaseReviewQueue();
        InvestigationTrail trail      = new InvestigationTrail();

        // 6. Build graph and calculate risk
        graph.buildConnectionGraph();
        riskEngine.scoreAllSuspects();

        // 7. Auto-queue HIGH risk suspects
        for (Suspect fraudster : suspectDB.values()) {
            if ("HIGH".equals(fraudster.getRiskLevel())) {
                caseQueue.addToQueue(fraudster);
            }
        }

        System.out.println(GREEN + " System Initialization Complete.\n" + RESET);

        // 8. Interactive Menu
        Scanner scanner = new Scanner(System.in);
        boolean running  = true;

        while (running) {

            printMenu();
            System.out.print("  Select option: ");
            String input = scanner.nextLine().trim();

            switch (input) {

                // Suspect Management
                case "1":
                    trail.navigateTo("SUSPECT_LIST");
                    suspectMgr.listAllSuspects();
                    break;

                case "2":
                    System.out.print("  Enter Suspect ID, name, or email to search: ");
                    String q = scanner.nextLine().trim();
                    trail.navigateTo("SEARCH:" + q);
                    List<Suspect> results = suspectMgr.searchSuspect(q);
                    if (results.isEmpty()) {
                        System.out.println("  No results found.");
                    } else {
                        for (int i = 0; i < results.size(); i++) {
                            System.out.println("  " + results.get(i));
                        }
                    }
                    break;

                case "3":
                    System.out.print("  Enter Suspect ID to view profile: ");
                    int profileId = parseIntSafe(scanner.nextLine());
                    trail.navigateTo("PROFILE:" + profileId);
                    suspectMgr.viewSuspectProfile(profileId);
                    break;

                case "4":
                    System.out.println("  Add Suspect - enter details:");
                    System.out.print("  Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("  Email: ");
                    String email = scanner.nextLine().trim();
                    System.out.print("  Phone: ");
                    String phone = scanner.nextLine().trim();
                    System.out.print("  Address: ");
                    String addr = scanner.nextLine().trim();
                    System.out.print("  IP Address: ");
                    String ip = scanner.nextLine().trim();
                    System.out.print("  Age: ");
                    int age = parseIntSafe(scanner.nextLine());
                    System.out.print("  Balance: $");
                    double balance = parseDoubleSafe(scanner.nextLine());
                    suspectMgr.addSuspect(name, email, phone, addr, ip, age, balance);
                    break;

                // Transaction Management
                case "5":
                    System.out.print("  Enter Suspect ID to view transactions: ");
                    int txnSuspectId = parseIntSafe(scanner.nextLine());
                    trail.navigateTo("TRANSACTIONS:" + txnSuspectId);
                    List<Transaction> txns = txnMgr.getTransactionHistory(txnSuspectId);
                    if (txns.isEmpty()) {
                        System.out.println("  No transactions found.");
                    } else {
                        for (int i = 0; i < txns.size(); i++) {
                            System.out.println("  " + txns.get(i));
                        }
                    }
                    break;

                // Connection Graph
                case "6":
                    trail.navigateTo("CONNECTION_GRAPH");
                    graph.displayConnectionGraph();
                    break;

                case "7":
                    System.out.print("  Enter Suspect ID to view connections: ");
                    int connId = parseIntSafe(scanner.nextLine());
                    trail.navigateTo("CONNECTIONS:" + connId);
                    List<ConnectionGraph.Connection> conns = graph.getConnectionsForSuspect(connId);
                    if (conns.isEmpty()) {
                        System.out.println("  No connections found.");
                    } else {
                        for (int i = 0; i < conns.size(); i++) {
                            System.out.println(conns.get(i));
                        }
                    }
                    break;

                // Risk Engine
                case "8":
                    trail.navigateTo("RISK_RANKINGS");
                    riskEngine.viewSortedSuspectList();
                    break;

                case "9":
                    System.out.print("  Recalculate all risk scores? (y/n): ");
                    String recalc = scanner.nextLine().trim();
                    if (recalc.equalsIgnoreCase("y")) {
                        riskEngine.scoreAllSuspects();
                        System.out.println("  Scores updated.");
                    }
                    break;

                // Case Queue
                case "10":
                    trail.navigateTo("CASE_QUEUE");
                    caseQueue.viewQueueStatus();
                    break;

                case "11":
                    caseQueue.reviewNextCase();
                    break;

                case "12":
                    System.out.print("  Enter Suspect ID to escalate: ");
                    int escId = parseIntSafe(scanner.nextLine());
                    caseQueue.escalateCase(escId);
                    break;

                // Dispute Management
                case "13":
                    System.out.print("  Suspect ID: ");
                    int dspSuspectId = parseIntSafe(scanner.nextLine());
                    System.out.print("  Transaction ID: ");
                    int dspTxnId = parseIntSafe(scanner.nextLine());
                    System.out.print("  Date opened (YYYY-MM-DD): ");
                    String dateOpened = scanner.nextLine().trim();
                    disputeMgr.addDispute(dspSuspectId, dspTxnId, dateOpened);
                    break;

                case "14":
                    System.out.print("  Dispute ID: ");
                    int did = parseIntSafe(scanner.nextLine());
                    System.out.print("  Resolution (LOSS / NOT_AT_FAULT / FRAUD_CONFIRMED): ");
                    String resolution = scanner.nextLine().trim().toUpperCase();
                    System.out.print("  Date closed (YYYY-MM-DD): ");
                    String dateClosed = scanner.nextLine().trim();
                    disputeMgr.resolveDispute(did, resolution, dateClosed);
                    break;

                // Investigation Trail
                case "15":
                    trail.viewTrail();
                    break;

                case "16":
                    trail.goBack();
                    break;

                // Shared Identifier Lookup
                case "17":
                    System.out.print("  Enter value to search (IP, email, address, or name): ");
                    String val  = scanner.nextLine().trim();
                    System.out.print("  Type (IP / EMAIL / ADDRESS / NAME): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    List<Integer> matches = graph.findSharedIdentifier(val, type);
                    System.out.println("  Suspects sharing this " + type + ": " + matches);
                    break;

                // Fraud Rules — view all loaded rules
                case "18":
                    trail.navigateTo("FRAUD_RULES");
                    ruleEngine.displayAllRules();
                    break;

                // Fraud Rules — view elder protection rules only
                case "19":
                    trail.navigateTo("ELDER_RULES");
                    ruleEngine.displayElderRules();
                    break;

                // Exit
                case "0":
                    System.out.println("\n  Exiting FraudOps...");
                    System.out.println("  Goodbye.\n");
                    running = false;
                    break;

                default:
                    System.out.println("  Invalid option. Try again.");
                    break;
            }
        }

        scanner.close();
    }

    // Main Menu
    private static void printMenu() {

        System.out.println();
        System.out.println(CYAN + "┌─────────────────────────────────────────┐");
        System.out.println("│              MAIN MENU                  │");
        System.out.println("├──────────────────────┬──────────────────┤");
        System.out.println("│  SUSPECTS            │  RISK & CASES    │");
        System.out.println("│  1. List All         │  8. Risk Rankings│");
        System.out.println("│  2. Search           │  9. Recalculate  │");
        System.out.println("│  3. View Profile     │  10. Case Queue  │");
        System.out.println("│  4. Add Suspect      │  11. Review Next │");
        System.out.println("│                      │  12. Escalate    │");
        System.out.println("├──────────────────────┼──────────────────┤");
        System.out.println("│  TRANSACTIONS        │  DISPUTES        │");
        System.out.println("│  5. View History     │  13. Add Dispute │");
        System.out.println("│                      │  14. Resolve     │");
        System.out.println("├──────────────────────┼──────────────────┤");
        System.out.println("│  CONNECTIONS         │  NAVIGATION      │");
        System.out.println("│  6. Full Graph       │  15. View Trail  │");
        System.out.println("│  7. By Suspect       │  16. Go Back     │");
        System.out.println("│  17. Lookup Value    │                  │");
        System.out.println("├──────────────────────┼──────────────────┤");
        System.out.println("│  FRAUD RULES                            │");
        System.out.println("│  18. View All Rules  │  19. Elder Rules │");
        System.out.println("├──────────────────────┴──────────────────┤");
        System.out.println("│  0. Exit                                │");
        System.out.println("└─────────────────────────────────────────┘" + RESET);
    }

    // Safe Parsing Helpers
    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}