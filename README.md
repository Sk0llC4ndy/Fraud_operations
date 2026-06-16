# Fraud Detection System
**Data Structures Final Project — Java Console Application**

A Java console application that detects financial fraud, elder financial abuse, and coordinated fraud rings by analyzing cardholder and transaction data loaded from CSV files.

---

## Overview

This project was built to apply core data structures concepts to a real-world fraud operations context. The system ingests cardholder and transaction records, evaluates them against a configurable rule set, scores each suspect by risk level, and surfaces the highest-priority cases for investigator review — mirroring workflows used in actual fraud operations environments.

---

## Features

### Suspect Management
Search, add, update, and remove cardholder profiles. Each profile aggregates transaction history, dispute records, risk scores, and network connections into a single view for investigators.

### Transaction Analysis
Incoming transactions are automatically routed through online and in-person validation checks. Flagged transactions are queued for review and contribute directly to a suspect's risk score. Checks include high-value amounts, odd-hours activity, card-not-present thresholds, billing/shipping mismatches, and authorization stuffing patterns.

### Elder Financial Abuse Detection
A dedicated rule set targets accounts belonging to cardholders aged 65 and older. The system flags patterns associated with third-party exploitation — caretaker relationships, dispute abuse, cash-equivalent transfers, and overnight wire activity — and escalates those cases automatically.

### Hash-Based Profile Indexing
Suspects are indexed across four shared-attribute dimensions: IP address, email, physical address, and name. Normalization and hashing allow the system to instantly surface all accounts linked to the same identifier, even with minor formatting differences in the raw data.

### Connection Graph & Fraud Ring Detection
Shared identifiers between suspects are used to build a connection graph. A breadth-first traversal detects clusters of linked accounts, revealing coordinated fraud rings that would not be visible from individual profile reviews.

### Risk Scoring & Prioritization
Each suspect receives a weighted risk score based on their flagged transactions, dispute history, financial losses, and network connections. Suspects are sorted by score using a custom merge sort implementation and ranked for investigator assignment.

### Case Review Queue
High-risk suspects are automatically placed into a priority queue so investigators always work the most critical case first. Cases can be escalated, reassigned, or closed, and the queue updates in real time as new risk data comes in.

### Investigation Trail
Every record an investigator navigates to is logged to a session stack. Investigators can step backward through their path, review the full trail for audit purposes, or reset to the main menu — supporting both active investigation and post-session review.

### Fraud Rule Engine
Detection logic is driven by 28 configurable rules loaded from a CSV file at startup. Rules define the field monitored, the condition and threshold, the flag type applied, and the risk score contribution. This keeps detection logic external to the codebase and easy to extend without modifying source code.

---

## Requirements

| Requirement | Version |
|---|---|
| Java JDK | 21 (LTS) or 25 |
| VS Code Extension | Extension Pack for Java |

---

## How to Run

CSV file paths are configured in `src/main/resources/config.properties`:

```properties
cardholder.csv.path=data/data-structures-final/data/cardholders.csv
transaction.csv.path=data/data-structures-final/data/transactions.csv
rules.csv.path=data/data-structures-final/data/fraud_detection_rules.csv
```

> **Note:** Use forward slashes in `config.properties` — backslashes are escape characters in `.properties` files and will cause silent misreads.

If the config file is missing, the application falls back to default hardcoded paths and launches normally.

---

## Built-In Test Scenario — Elder Abuse Ring

The included dataset contains a pre-built elder abuse scenario for demonstration purposes:

| Suspect ID | Name | Age | Role |
|---|---|---|---|
| 2001 | Dorothy Haines | 78 | Victim — elder cardholder |
| 2002 | Ray Haines | 31 | Primary abuser — grandson/caretaker, same address |
| 2003 | Tammy Ogle | 28 | Accomplice — receives shipments, shares IP |
| 2004 | Darnell Cross | 34 | Accomplice — mule account |
| 2005 | Margaret Ellis | 71 | Clean elder — control group |
| 2006 | Samuel Price | 45 | Clean cardholder — control group |

**Suggested walkthrough:**
1. View Risk Rankings — Dorothy and Ray appear at the top
2. Open Dorothy's profile — flagged transactions and Ray's name are visible
3. View the full Connection Graph — the fraud ring is surfaced
4. Open the Case Queue — HIGH risk suspects are pre-loaded
5. View the Investigation Trail — full session path is logged

---

## Project Structure

```
data-structures-final/
├── data/
│   ├── cardholders.csv
│   ├── transactions.csv
│   └── fraud_detection_rules.csv
├── src/
│   └── main/
│       ├── java/com/fraudoperations/
│       │   ├── Main.java
│       │   ├── models/
│       │   │   ├── Suspect.java
│       │   │   ├── Transaction.java
│       │   │   ├── Dispute.java
│       │   │   └── FraudRule.java
│       │   ├── managers/
│       │   │   ├── CSVLoader.java
│       │   │   ├── SuspectManager.java
│       │   │   ├── TransactionManager.java
│       │   │   ├── DisputeManager.java
│       │   │   └── CaseReviewQueue.java
│       │   └── systems/
│       │       ├── HashIndexSystem.java
│       │       ├── ConnectionGraph.java
│       │       ├── RiskEngine.java
│       │       ├── InvestigationTrail.java
│       │       └── FraudRuleEngine.java
│       └── resources/
│           └── config.properties
```