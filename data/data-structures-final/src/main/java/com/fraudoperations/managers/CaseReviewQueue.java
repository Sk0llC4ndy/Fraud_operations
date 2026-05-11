package com.fraudoperations.managers;

import com.fraudoperations.models.Suspect;

import java.util.*;

/**
 * Feature 9 - Case Review Queue
 * Manages flagged suspects waiting for investigator review.
 * Uses a PriorityQueue ordered by risk score (highest first, descending).
 *
 * Data structures used:
 *   PriorityQueue<Case>        - max-heap ordered by priority (risk score)
 *   LinkedHashMap<Integer,Case> - O(1) case lookup by suspectID (Map)
 *   ArrayList<Case>            - temporary sorted list for display
 */
public class CaseReviewQueue {

    // Reusable Comparator — orders cases highest priority first (max-heap behavior)
    private static final Comparator<Case> PRIORITY_COMPARATOR =
            new Comparator<Case>() {
                @Override
                public int compare(Case a, Case b) {
                    return Integer.compare(b.priority, a.priority);
                }
            };

    /**
     * Wraps a Suspect with queue-specific investigation metadata.
     * Custom inner class used exclusively by CaseReviewQueue.
     */
    public static class Case {

        /** The suspect this case is tracking. */
        public Suspect fraudster;

        /** Current status: PENDING, IN_REVIEW, ESCALATED, or CLOSED. */
        public String caseStatus;

        /** Name of the assigned investigator. Defaults to unassigned. */
        public String assignedInvestigator;

        /** Priority value (1-5) derived from the suspect's risk score. */
        public int priority;

        /**
         * Creates a new Case for the given suspect with PENDING status.
         *
         * @param fraudster the suspect to open a case for
         */
        public Case(Suspect fraudster) {
            this.fraudster            = fraudster;
            this.caseStatus           = "PENDING";
            this.assignedInvestigator = "unassigned";
            this.priority             = (int) Math.round(fraudster.getRiskScore());
        }

        /**
         * Returns a formatted one-line summary of the case.
         *
         * @return case summary string
         */
        @Override
        public String toString() {
            return String.format(
                    "Case [%d] %s | Priority: %d | Status: %s | Investigator: %s",
                    fraudster.getSuspectID(),
                    fraudster.getName(),
                    priority,
                    caseStatus,
                    assignedInvestigator);
        }
    }

    /**
     * Max-heap PriorityQueue: highest priority (risk score) dequeued first.
     * Uses PRIORITY_COMPARATOR to reverse natural order.
     */
    private final PriorityQueue<Case> queue = new PriorityQueue<>(PRIORITY_COMPARATOR);

    /**
     * Secondary lookup map for O(1) access by suspectID.
     * Used for escalation and closure without scanning the queue.
     * Backed by LinkedHashMap to preserve insertion order.
     */
    private final Map<Integer, Case> caseMap = new LinkedHashMap<>();

    /**
     * Wraps the suspect in a Case and inserts it into the PriorityQueue.
     * Does nothing if the suspect is already in the queue.
     *
     * @param fraudster the suspect to enqueue
     */
    public void addToQueue(Suspect fraudster) {
        if (caseMap.containsKey(fraudster.getSuspectID())) {
            System.out.println("Suspect " + fraudster.getSuspectID() + " already in queue.");
            return;
        }
        Case event = new Case(fraudster);
        queue.add(event);
        caseMap.put(fraudster.getSuspectID(), event);
        System.out.println("Added to queue: " + event);
    }

    /**
     * Polls the highest-priority case from the PriorityQueue
     * and sets its status to IN_REVIEW.
     *
     * @return the next Case to review, or null if the queue is empty
     */
    public Case reviewNextCase() {
        Case event = queue.poll();
        if (event == null) {
            System.out.println("Queue is empty.");
            return null;
        }
        event.caseStatus = "IN_REVIEW";
        System.out.println("Now reviewing: " + event);
        return event;
    }

    /**
     * Escalates a case by removing it from the queue, setting its priority
     * to the maximum value (5), and re-inserting it so it rises to the top.
     *
     * @param suspectID the suspect whose case to escalate
     */
    public void escalateCase(int suspectID) {
        Case event = caseMap.get(suspectID);
        if (event == null) {
            System.out.println("Case not found.");
            return;
        }
        queue.remove(event);
        event.priority   = 5;
        event.caseStatus = "ESCALATED";
        queue.add(event);
        System.out.println("Escalated: " + event);
    }

    /**
     * Removes a resolved case from both the PriorityQueue and the lookup Map.
     *
     * @param suspectID the suspect whose case to close
     */
    public void closeCaseInQueue(int suspectID) {
        Case event = caseMap.remove(suspectID);
        if (event == null) {
            System.out.println("Case not found.");
            return;
        }
        queue.remove(event);
        event.caseStatus = "CLOSED";
        System.out.println("Closed case for suspect " + suspectID);
    }

    /**
     * Copies the PriorityQueue to a temporary ArrayList, sorts it by priority,
     * and displays all pending cases to the console.
     */
    public void viewQueueStatus() {
        if (queue.isEmpty()) {
            System.out.println("Review queue is empty.");
            return;
        }

        List<Case> sorted = new ArrayList<>(queue);
        sorted.sort(PRIORITY_COMPARATOR);

        System.out.println("=".repeat(70));
        System.out.println("  CASE REVIEW QUEUE (" + sorted.size() + " pending)");
        System.out.println("=".repeat(70));
        for (int i = 0; i < sorted.size(); i++) {
            System.out.println("  " + sorted.get(i));
        }
        System.out.println("=".repeat(70));
    }

    /**
     * Returns the number of cases currently in the queue.
     *
     * @return queue size
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns true if the queue is empty.
     *
     * @return true if no cases are pending
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}