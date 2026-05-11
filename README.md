# Fraud Detection System
### Data Structures Final Project — `com.fraudoperations`

A Java console application that detects financial fraud, elder financial abuse, and coordinated fraud rings by loading cardholder and transaction data from CSV files and analyzing it using purpose-built data structures.

---

## Project Goals

1. **Detect elder financial abuse** — identify transactions on accounts where the cardholder is 65+ that show signs of third-party exploitation, including caretaker patterns, dispute abuse, and midnight wire transfers.
2. **Expose fraud rings** — build a connection graph from shared IP addresses, email addresses, physical addresses, and names to surface clusters of coordinated suspects.
3. **Prioritize investigations** — score every suspect using a weighted risk formula and maintain a priority queue so investigators always work the highest-risk case first.
4. **Track investigation paths** — record every record an investigator navigates to so sessions can be reviewed, replayed, and audited.
5. **Demonstrate data structures in practice** — every core Java data structure required by the assignment is used meaningfully, not artificially.

---

## Requirements

| Requirement | Version |
|---|---|
| Java JDK | 21 (LTS) or 25 |
| VS Code Extension | Extension Pack for Java |

---

## Project Structure

```
data-structures-final/
├── data/
│   ├── cardholders.csv
│   ├── transactions.csv
│   └── fraud_detection_rules.csv
├── src/
│   ├── main/
│   │   ├── java/com/fraudoperations/
│   │   │   ├── Main.java
│   │   │   ├── models/
│   │   │   │   ├── Suspect.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── Dispute.java
│   │   │   │   └── FraudRule.java
│   │   │   ├── managers/
│   │   │   │   ├── CSVLoader.java
│   │   │   │   ├── SuspectManager.java
│   │   │   │   ├── TransactionManager.java
│   │   │   │   ├── DisputeManager.java
│   │   │   │   └── CaseReviewQueue.java
│   │   │   └── systems/
│   │   │       ├── HashIndexSystem.java
│   │   │       ├── ConnectionGraph.java
│   │   │       ├── RiskEngine.java
│   │   │       ├── InvestigationTrail.java
│   │   │       └── FraudRuleEngine.java
│   │   └── resources/
│   │       └── config.properties
│   └── test/java/com/fraudoperations/
```

---

## How to Run

CSV file paths are loaded from `src/main/resources/config.properties` at startup. Update these paths if your data folder is in a different location:

```properties
cardholder.csv.path=data/data-structures-final/data/cardholders.csv
transaction.csv.path=data/data-structures-final/data/transactions.csv
rules.csv.path=data/data-structures-final/data/fraud_detection_rules.csv
```

> **Note:** Use forward slashes (`/`) in `config.properties` — backslashes are escape characters in `.properties` files and will cause silent misreads (e.g. `\t` is interpreted as a tab).

If `config.properties` is missing or cannot be loaded, `Main.java` falls back to these defaults:

```java
"data\\data-structures-final\\data\\cardholders.csv"
"data\\data-structures-final\\data\\transactions.csv"
"data\\data-structures-final\\data\\fraud_detection_rules.csv"
```

---

## Startup Sequence

On launch, `Main.java` executes the following steps in order before presenting the interactive menu:

1. Loads cardholders and transactions from CSV via `CSVLoader`
2. Loads all fraud detection rules from `fraud_detection_rules.csv` via `FraudRuleEngine`
3. Evaluates all 28 rules against every transaction (including elder abuse rules TXN-021 through TXN-028)
4. Indexes all suspects into the hash system via `HashIndexSystem`
5. Builds the connection graph via `ConnectionGraph`
6. Calculates risk scores for all suspects via `RiskEngine`
7. Auto-queues all HIGH risk suspects into `CaseReviewQueue`

---

## Data Structures Used

| Data Structure | Class | Purpose |
|---|---|---|
| **ArrayList** (List) | `Suspect.java` | `List<Transaction>` transaction history per suspect |
| **ArrayList** (List) | `Suspect.java` | `List<Dispute>` dispute history per suspect |
| **ArrayList** (List) | `Suspect.java` | `List<Integer>` connected suspect IDs |
| **ArrayList** (List) | `SuspectManager.java` | Search results returned from `searchSuspect()` |
| **ArrayList** (List) | `CSVLoader.java` | Flat list of all loaded transactions |
| **ArrayList** (List) | `FraudRuleEngine.java` | Ordered list of all loaded `FraudRule` objects |
| **ArrayList** (List) | `FraudRuleEngine.java` | Internal CSV field parser buffer |
| **LinkedList** (Queue) | `TransactionManager.java` | `Queue<Transaction> reviewQueue` — flagged transactions pending review |
| **LinkedList** (Queue) | `ConnectionGraph.java` | BFS traversal queue in `detectClusters()` |
| **LinkedHashMap** (Map) | `CSVLoader.java` | `LinkedHashMap<Integer, Suspect>` preserves insertion order |
| **LinkedHashMap** (Map) | `DisputeManager.java` | Dispute lookup table |
| **LinkedHashMap** (Map) | `CaseReviewQueue.java` | O(1) case lookup by suspectID |
| **HashMap** (Map) | `HashIndexSystem.java` | Four index maps: IP, email, address, name |
| **HashMap** (Map) | `ConnectionGraph.java` | Adjacency list for the connection graph |
| **PriorityQueue** | `CaseReviewQueue.java` | Max-heap — highest risk score dequeued first |
| **Stack** | `InvestigationTrail.java` | `Stack<String>` navigation history with push/pop/peek |
| **Custom Object** | `ConnectionGraph.java` | `Connection` inner class — two suspect IDs, type, shared value, date |
| **Custom Object** | `CaseReviewQueue.java` | `Case` inner class — wraps Suspect with status, priority, investigator |
| **Custom Object** | `FraudRule.java` | Model class — holds all 12 fields of a loaded fraud detection rule |
| **Comparable + Merge Sort** | `Suspect.java` + `RiskEngine.java` | `Suspect implements Comparable<Suspect>`; `RiskEngine` sorts using custom merge sort |

---

## Feature Breakdown & Function Reference

### Feature 1 — Suspect Management (`SuspectManager.java`)

| Method | Description |
|---|---|
| `addSuspect()` | Creates a Suspect, stores in Map, indexes in all four hash maps |
| `searchSuspect(query)` | Case-insensitive search by ID, name, or email; returns ArrayList |
| `updateSuspect(id, field, value)` | Updates one field and re-indexes the suspect |
| `viewSuspectProfile(id)` | Prints all data: identifiers, balance, risk, transactions, disputes, connections |
| `linkSuspects(idA, idB, reason)` | Creates a manual bidirectional connection between two suspects |
| `removeSuspect(id)` | Removes from Map and cleans all four hash indexes |

---

### Feature 2 — Transaction Management (`TransactionManager.java`)

| Method | Description |
|---|---|
| `addTransaction()` | Creates a Transaction, links to suspect's ArrayList, routes to validation |
| `categorizeTransaction(t)` | Routes to online or in-person checks based on `isOnline` |
| `flagTransaction(t, reason)` | Sets `isFlagged = true`, appends reason to pipe-delimited `flagReason` |
| `getTransactionHistory(id)` | Returns the suspect's transaction ArrayList |
| `removeTransaction(id, txnId)` | Removes a transaction from the suspect's ArrayList |
| `getReviewQueue()` | Returns the LinkedList-backed Queue of flagged transactions |

---

### Feature 3 & 4 — Online & In-Person Checks (`TransactionManager.java`)

Online checks: name/email/address mismatch, amount > $5,000, CNP > $1,500, auth stuffing (> 2 attempts), odd hours (12 AM–5 AM), round amounts ($500 multiples).

In-person checks: name/email/address mismatch, amount > $5,000.

Both channels output reports via `generateOnlineReport()` and `generateInPersonReport()`.

---

### Feature 5 — Hash Indexing System (`HashIndexSystem.java`)

| Method | Description |
|---|---|
| `normalizeIP/Email/Address/Name()` | Strips punctuation, lowercases, standardizes abbreviations |
| `hashIP/Email/Address/Name()` | Polynomial rolling hash: `hash = (hash * 31 + c) % 9973` |
| `indexSuspect(s)` | Inserts suspect ID into all four HashMaps in one call |
| `removeSuspect(s)` | Removes suspect ID from all four HashMaps |
| `lookupByIP/Email/Address/Name()` | Returns ArrayList of suspect IDs at that hash key |
| `isSharedIP/Email/Address()` | Returns true if more than one suspect maps to the same key |

---

### Feature 6 — Dispute & Abuse Tracking (`DisputeManager.java`)

| Method | Description |
|---|---|
| `addDispute(suspectId, txnId, date)` | Opens dispute, stores in LinkedHashMap, appends to suspect's ArrayList |
| `resolveDispute(id, resolution, date)` | Sets RESOLVED, records outcome, sets `wasLoss` flag |
| `updateDisputeStatus(id, status)` | Moves dispute through OPEN → UNDER_REVIEW → RESOLVED |
| `getDisputeHistory(id)` | Returns suspect's dispute ArrayList |
| `countOpenDisputes(id)` | Count of unresolved disputes |
| `countResolvedDisputes(id)` | Count of resolved disputes |
| `getDisputeOutcomeSummary(id)` | Prints LOSS / NOT_AT_FAULT / FRAUD_CONFIRMED counts |

---

### Feature 7 — Connection Graph (`ConnectionGraph.java`)

| Method | Description |
|---|---|
| `buildConnectionGraph()` | Iterates four index maps, creates Connection objects for shared keys, builds adjacency list |
| `getConnectionsForSuspect(id)` | Returns all Connection objects for a suspect |
| `findSharedIdentifier(value, type)` | Returns all suspect IDs sharing a given IP/email/address/name |
| `getConnectionStrength(idA, idB)` | Counts shared data points between two suspects |
| `detectClusters()` | BFS using LinkedList Queue — finds all connected fraud rings |
| `displayConnectionGraph()` | Prints full graph with edges and identified rings |

`Connection` (Custom Inner Class): stores `suspectID_A`, `suspectID_B`, `connectionType`, `sharedValue`, `dateDetected`.

---

### Feature 8 — Risk Scoring & Sorting (`RiskEngine.java`)

Weighted score formula (normalized to 1.0–5.0):

| Factor | Weight |
|---|---|
| Each flagged transaction | +1.5 |
| Each dispute | +1.0 |
| Each financial loss | +2.0 |
| Each connection | +0.5 |
| Shared IP | +3.0 |
| Shared email | +2.0 |
| Shared address | +2.5 |

Risk bands: **LOW** < 2.0 · **MEDIUM** 2.0–3.4 · **HIGH** ≥ 3.5

| Method | Description |
|---|---|
| `calculateRiskScore(s)` | Applies weights, normalizes to 1–5, sets score and level on Suspect |
| `scoreAllSuspects()` | Scores every suspect in the Map |
| `sortSuspectsByRisk()` | Returns ArrayList sorted via custom merge sort using `Suspect.compareTo()` |
| `viewSortedSuspectList()` | Prints ranked table highest to lowest |

`Suspect implements Comparable<Suspect>` orders by `riskScore` descending, satisfying the Comparable requirement.

---

### Feature 9 — Case Review Queue (`CaseReviewQueue.java`)

| Method | Description |
|---|---|
| `addToQueue(s)` | Wraps in Case, adds to PriorityQueue and LinkedHashMap |
| `reviewNextCase()` | Polls max-heap (highest priority first), sets IN_REVIEW |
| `escalateCase(id)` | Removes, sets priority to 5, re-inserts to rise to top |
| `closeCaseInQueue(id)` | Removes from PriorityQueue and Map |
| `viewQueueStatus()` | Copies to ArrayList, sorts, prints all pending cases |

`Case` (Custom Inner Class): wraps Suspect with `caseStatus`, `priority`, and `assignedInvestigator`.

---

### Feature 10 — Investigation Trail (`InvestigationTrail.java`)

| Method | Description |
|---|---|
| `navigateTo(destination)` | Pushes current view onto Stack, moves to new view |
| `goBack()` | Pops Stack, returns to previous view |
| `peekPrevious()` | Returns top of Stack without removing it |
| `viewTrail()` | Clones and reverses Stack to print full path in order |
| `clearTrail()` | Resets Stack and returns to MAIN_MENU |

---

### Feature 11 — Fraud Rule Engine (`FraudRuleEngine.java` + `FraudRule.java`)

Rules are loaded from `fraud_detection_rules.csv` at startup into an `ArrayList<FraudRule>`. Each rule is evaluated against every transaction in the database. Results feed directly into `RiskEngine` scoring and `ConnectionGraph` detection.

#### `FraudRule.java` — Model (`com.fraudoperations.models`)

Represents one row from `fraud_detection_rules.csv`. Holds all 12 fields of a rule definition.

| Field | Type | Description |
|---|---|---|
| `ruleID` | String | Rule identifier (e.g. TXN-021) |
| `ruleName` | String | Human-readable rule name |
| `category` | String | Grouping (e.g. Elder Protection, Amount Threshold) |
| `fieldMonitored` | String | Fields this rule checks |
| `condition` | String | Plain-English condition description |
| `operator` | String | Comparison operator: `>`, `>=`, `==`, `!=`, `IN`, `BETWEEN` |
| `value` | String | Threshold or comparison value |
| `flagType` | String | Flag code applied when triggered (e.g. `ELDER_ABUSE_INDICATOR`) |
| `riskScore` | int | Risk score contribution (1–5 scale) |
| `actionTriggered` | String | Recommended action on trigger |
| `onlineOnly` | boolean | True if rule applies only to online transactions |
| `notes` | String | Investigator reference notes |

#### `FraudRuleEngine.java` — System (`com.fraudoperations.systems`)

| Method | Description |
|---|---|
| `loadRules()` | Reads `fraud_detection_rules.csv`, parses all rows into `ArrayList<FraudRule>`, skips header |
| `evaluateAll(suspectDB)` | Iterates every suspect and every transaction; calls `evaluateTransaction()` on each pair |
| `evaluateTransaction(txn, suspect)` | Evaluates all rules against one transaction; skips online-only rules for in-person; prevents double-flagging |
| `checkRule(rule, txn, suspect)` | Switch on `ruleID` — maps each rule to its programmatic condition check |
| `applyFlag(txn, flagType)` | Appends flag code to pipe-delimited `flagReason` and sets `isFlagged = true` |
| `alreadyFlagged(txn, flagType)` | Returns true if the flag code is already present — prevents duplicates |
| `displayAllRules()` | Prints formatted table of all 28 loaded rules |
| `displayElderRules()` | Prints only rules with category `Elder Protection` with full detail |
| `getRules()` | Returns the full `ArrayList<FraudRule>` |
| `getRuleByID(ruleID)` | Returns a single rule by ID, or null if not found |

**Rules implemented in `checkRule()`:**

| Rule ID | Name | Condition |
|---|---|---|
| TXN-001 | High Value Transaction | Amount > $5,000 |
| TXN-003 | Round Amount | Amount is a multiple of $500 |
| TXN-004 | New Device / IP | Online transaction from IP not matching suspect's on-file IP |
| TXN-006 | Billing/Shipping Mismatch | Billing address ≠ shipping address |
| TXN-009 | Odd Hours | Transaction between 12 AM and 5 AM |
| TXN-010 | CNP High Amount | Online transaction > $1,500 |
| TXN-011 | Auth Stuffing | More than 2 failed auth attempts |
| TXN-014 | Name Mismatch | Name on transaction ≠ cardholder name on file |
| TXN-015 | Duplicate Transaction | Same amount at same merchant as a prior transaction |
| TXN-019 | Unusual Merchant | First-ever transaction in this merchant category for the suspect |
| TXN-021 | Elder Cardholder Flag | Cardholder age ≥ 65 with any flagged activity |
| TXN-022 | Caretaker Pattern | Different name but same address as elder cardholder |
| TXN-023 | Third-Party Dispute | Dispute filed by someone other than the elder cardholder |
| TXN-024 | Dispute Accumulation | Elder account with more than 2 disputes on file |
| TXN-025 | Elder Odd Hours | Transaction on elder account between 10 PM and 6 AM |
| TXN-026 | Cash Equivalent — Elder | Wire transfer, gift card, or cash advance on elder account |
| TXN-027 | Email Mismatch — Elder | Email on transaction ≠ elder cardholder email on file |
| TXN-028 | Ships to Non-Cardholder Address | Online elder account transaction ships to non-cardholder address |

Rules TXN-002, TXN-005, TXN-007, TXN-008, TXN-012, TXN-013, TXN-016, TXN-017, TXN-018, and TXN-020 require external data (geo-IP, velocity windows, blacklists) and are handled by `TransactionManager` or noted for future extension.

---

## Elder Abuse Scenario (Built-In Test Data)

| Suspect ID | Name | Age | Role |
|---|---|---|---|
| 2001 | Dorothy Haines | 78 | **VICTIM** — elder cardholder |
| 2002 | Ray Haines | 31 | **PRIMARY ABUSER** — grandson/caretaker, same address |
| 2003 | Tammy Ogle | 28 | **ACCOMPLICE** — receives shipments, shares IP |
| 2004 | Darnell Cross | 34 | **ACCOMPLICE** — mule account |
| 2005 | Margaret Ellis | 71 | Clean elder — control group |
| 2006 | Samuel Price | 45 | Clean cardholder — control group |

**Suggested investigation path:**
1. Option **8** — Risk Rankings (Dorothy and Ray appear at top)
2. Option **3** → `2001` — Dorothy's profile shows flagged transactions and Ray's name
3. Option **6** — Full Connection Graph reveals the fraud ring
4. Option **7** → `2001` — Dorothy's direct connections to Ray, Tammy, Darnell
5. Option **10** — Case Queue shows HIGH risk suspects pre-loaded
6. Option **15** — View Trail shows the full investigation path taken

---

## Fraud Flag Reference

| Flag | Trigger |
|---|---|
| `ELDER_ABUSE_INDICATOR` | Age ≥ 65 + suspicious pattern |
| `CARETAKER_PATTERN` | Different name, same address as elder |
| `DISPUTE_ABUSE` | Dispute filed by someone other than the cardholder |
| `NAME_MISMATCH` | Name on transaction ≠ name on file |
| `EMAIL_MISMATCH` | Email on transaction ≠ email on file |
| `ADDRESS_MISMATCH` | Billing ≠ shipping, or billing ≠ cardholder address |
| `AMOUNT_HIGH` | Transaction > $5,000 |
| `CNP_HIGH_AMOUNT` | Online transaction > $1,500 |
| `ODD_HOURS` | Transaction between 12 AM and 5 AM |
| `ROUND_AMOUNT` | Amount is a multiple of $500 |
| `VELOCITY_HIGH` | > 5 transactions in a 10-minute window |
| `DUPLICATE_TXN` | Same amount at same merchant within 5 minutes |
| `AUTH_STUFFING` | > 2 failed auth attempts before approval |
| `GEO_MISMATCH` | IP country ≠ billing address country |
| `BLACKLISTED_IP` | IP on fraud blacklist |
| `NEW_DEVICE_ONLINE` | Online purchase from unrecognized IP |
| `UNUSUAL_MERCHANT` | First transaction ever in this merchant category for the suspect |
| `CASH_EQUIVALENT_ELDER` | Wire transfer, gift card, or cash advance on elder account |
| `THIRD_PARTY_DISPUTE` | Dispute opened on elder account by someone other than cardholder |

## Author
Tyler Ward
Fraud Operations - Data Structures Final Project