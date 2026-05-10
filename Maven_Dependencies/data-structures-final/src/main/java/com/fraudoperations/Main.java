package com.fraudoperations;

import com.fraudoperations.managers.*;
import com.fraudoperations.models.*;
import com.fraudoperations.systems.*;

import java.util.*;

/**
 * Fraud Detection Program — Entry Point
 */
public class Main {

    // ── ANSI Terminal Colors ──────────────────────────────────────────────────
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN  = "\u001B[36m";
    public static final String RESET = "\u001B[0m";

    // ── CSV Paths ─────────────────────────────────────────────────────────────
    private static final String CARDHOLDERS_CSV  = "data/cardholders.csv";
    private static final String TRANSACTIONS_CSV = "data/transactions.csv";

    public static void main(String[] args) {

        // ── Startup Banner ────────────────────────────────────────────────────
        System.out.println(GREEN + """

███████╗██████╗  █████╗ ██╗   ██╗██████╗  ██████╗ ██████╗ ███████╗
██╔════╝██╔══██╗██╔══██╗██║   ██║██╔══██╗██╔═══██╗██╔══██╗██╔════╝
█████╗  ██████╔╝███████║██║   ██║██║  ██║██║   ██║██████╔╝███████╗
██╔══╝  ██╔══██╗██╔══██║██║   ██║██║  ██║██║   ██║██╔═══╝ ╚════██║
██║     ██║  ██║██║  ██║╚██████╔╝██████╔╝╚██████╔╝██║     ███████║
╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚═╝     ╚══════╝

""" + RESET);

        System.out.println(CYAN + "════════════════════════════════════════════════════════════");
        System.out.println("        FRAUD DETECTION & INVESTIGATION SYSTEM");
        System.out.println("════════════════════════════════════════════════════════════" + RESET);

        System.out.println(" Loading suspect database...");
        System.out.println(" Loading transaction records...");
        System.out.println(" Building fraud connection graph...");
        System.out.println(" Calculating risk scores...");
        System.out.println(" Initializing investigation systems...");
        System.out.println();

        // 1. Load data from CSV 
        CSVLoader loader = new CSVLoader(CARDHOLDERS_CSV, TRANSACTIONS_CSV);

        Map<Integer, Suspect> suspectDB = loader.loadSuspects();
        List<Transaction> transactions = loader.loadTransactions();

        loader.linkTransactionsToSuspects(suspectDB, transactions);

        // 2. Build systems 
        HashIndexSystem hashIndex = new HashIndexSystem();
        for (Suspect fraudster : suspectDB.values()) {   
            hashIndex.indexSuspect(fraudster);
        }

        SuspectManager suspectMgr = new SuspectManager(suspectDB, hashIndex);
        TransactionManager txnMgr = new TransactionManager(suspectDB);
        DisputeManager disputeMgr = new DisputeManager(suspectDB);
        ConnectionGraph graph = new ConnectionGraph(hashIndex, suspectDB);
        RiskEngine riskEngine = new RiskEngine(suspectDB, hashIndex);
        CaseReviewQueue caseQueue = new CaseReviewQueue();
        InvestigationTrail trail = new InvestigationTrail();

        // 3. Build graph and calculate risk
        graph.buildConnectionGraph();
        riskEngine.scoreAllSuspects();

        // Auto-queue HIGH risk suspects
       for (Suspect fraudster : suspectDB.values()) {
            if ("HIGH".equals(fraudster.getRiskLevel())) {
            caseQueue.addToQueue(fraudster);
            }   
        }
        System.out.println(GREEN + " System Initialization Complete.\n" + RESET);

        // 4. Interactive Menu
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {

            printMenu();

            System.out.print("  Select option: ");
            String input = scanner.nextLine().trim();

            switch (input) {

                // ── Suspect Management ───────────────────────────────────────
                case "1" -> {
                    trail.navigateTo("SUSPECT_LIST");
                    suspectMgr.listAllSuspects();
                }

                case "2" -> {

                    System.out.print("  Enter Suspect ID, name, or email to search: ");

                    String q = scanner.nextLine().trim();

                    trail.navigateTo("SEARCH:" + q);

                    List<Suspect> results = suspectMgr.searchSuspect(q);

                    if (results.isEmpty()) {

                        System.out.println("  No results found.");

                    } else {

                        for (Suspect fraudster : results) {
                            System.out.println("  " + fraudster);
                        }
                    }
                }

                case "3" -> {
                    System.out.print("  Enter Suspect ID to view profile: ");

                    int id = parseIntSafe(scanner.nextLine());

                    trail.navigateTo("PROFILE:" + id);

                    suspectMgr.viewSuspectProfile(id);
                }

                case "4" -> {
                    System.out.println("  Add Suspect — enter details:");

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

                    System.out.print("  Job: ");
                    String job = scanner.nextLine().trim();

                    System.out.print("  Balance: $");
                    double balance = parseDoubleSafe(scanner.nextLine());

                    suspectMgr.addSuspect(
                            name,
                            email,
                            phone,
                            addr,
                            ip,
                            age,
                            job,
                            balance
                    );
                }

                // ── Transaction Management ───────────────────────────────────
                case "5" -> {
                    System.out.print("  Enter Suspect ID to view transactions: ");

                    int id = parseIntSafe(scanner.nextLine());

                    trail.navigateTo("TRANSACTIONS:" + id);

                    List<Transaction> txns = txnMgr.getTransactionHistory(id);

                    if (txns.isEmpty()) {
                        System.out.println("  No transactions found.");
                    } else {
                        txns.forEach(t -> System.out.println("  " + t));
                    }
                }

                // ── Connection Graph ─────────────────────────────────────────
                case "6" -> {
                    trail.navigateTo("CONNECTION_GRAPH");
                    graph.displayConnectionGraph();
                }

                case "7" -> {
                    System.out.print("  Enter Suspect ID to view connections: ");

                    int id = parseIntSafe(scanner.nextLine());

                    trail.navigateTo("CONNECTIONS:" + id);

                    List<ConnectionGraph.Connection> conns =
                            graph.getConnectionsForSuspect(id);

                    if (conns.isEmpty()) {
                        System.out.println("  No connections found.");
                    } else {
                        conns.forEach(System.out::println);
                    }
                }

                // ── Risk Engine ──────────────────────────────────────────────
                case "8" -> {
                    trail.navigateTo("RISK_RANKINGS");
                    riskEngine.viewSortedSuspectList();
                }

                case "9" -> {
                    System.out.print("  Recalculate all risk scores? (y/n): ");

                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {

                        riskEngine.scoreAllSuspects();

                        System.out.println("  Scores updated.");
                    }
                }

                // ── Case Queue ───────────────────────────────────────────────
                case "10" -> {
                    trail.navigateTo("CASE_QUEUE");
                    caseQueue.viewQueueStatus();
                }

                case "11" -> caseQueue.reviewNextCase();

                case "12" -> {
                    System.out.print("  Enter Suspect ID to escalate: ");

                    int id = parseIntSafe(scanner.nextLine());

                    caseQueue.escalateCase(id);
                }

                // ── Dispute Management ───────────────────────────────────────
                case "13" -> {
                    System.out.print("  Suspect ID: ");
                    int SuspectId = parseIntSafe(scanner.nextLine());

                    System.out.print("  Transaction ID: ");
                    int transactionId = parseIntSafe(scanner.nextLine());

                    System.out.print("  Date opened (YYYY-MM-DD): ");
                    String dateOpened = scanner.nextLine().trim();

                    disputeMgr.addDispute(SuspectId, transactionId, dateOpened);
                }

                case "14" -> {
                    System.out.print("  Dispute ID: ");
                    int disputeId = parseIntSafe(scanner.nextLine());

                    System.out.print(
                            "  Resolution (LOSS / NOT_AT_FAULT / FRAUD_CONFIRMED): "
                    );

                    String resolution = scanner.nextLine().trim().toUpperCase();

                    System.out.print("  Date closed (YYYY-MM-DD): ");
                    String dateClosed = scanner.nextLine().trim();

                    disputeMgr.resolveDispute(disputeId, resolution, dateClosed);
                }

                // ── Investigation Trail ──────────────────────────────────────
                case "15" -> trail.viewTrail();

                case "16" -> trail.goBack();

                // ── Shared Identifier Lookup ─────────────────────────────────
                case "17" -> {

                    System.out.print(
                            "  Enter value to search (IP, email, address, or name): "
                    );

                    String val = scanner.nextLine().trim();

                    System.out.print("  Type (IP / EMAIL / ADDRESS / NAME): ");

                    String type = scanner.nextLine().trim().toUpperCase();

                    List<Integer> matches =
                            graph.findSharedIdentifier(val, type);

                    System.out.println(
                            "  Suspects sharing this " + type + ": " + matches
                    );
                }

                // ── Exit ─────────────────────────────────────────────────────
                case "0" -> {
                    System.out.println("\n  Exiting FraudOps...");
                    System.out.println("  Goodbye.\n");

                    running = false;
                }

                default -> System.out.println("  Invalid option. Try again.");
            }
        }

        scanner.close();
    }
}

    // ── Main Menu ─────────────────────────────────────────────────────────────

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
        System.out.println("├──────────────────────┴──────────────────┤");
        System.out.println("│  0. Exit                                │");
        System.out.println("└─────────────────────────────────────────┘" + RESET);
    }

    // ── Safe Parsing Helpers ─────────────────────────────────────────────────

    private static int parseIntSafe(String s) {

        try {
            return Integer.parseInt(s.trim());
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double parseDoubleSafe(String s) {

        try {
            return Double.parseDouble(s.trim());
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }
    }