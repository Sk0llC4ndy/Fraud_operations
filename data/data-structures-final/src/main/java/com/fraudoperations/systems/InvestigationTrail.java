package com.fraudoperations.systems;

import java.util.Stack;

/**
 * Feature 10 - Investigation Trail
 * Tracks the navigation path an investigator takes through records.
 *
 * Every call to navigateTo() pushes the current view onto the Stack before
 * moving to the destination. goBack() pops the Stack to return. peekPrevious()
 * reads the top without removing it. viewTrail() clones and reverses the Stack
 * to print the path in chronological order.
 *
 * Data structures used:
 *   Stack<String> - navigation history with push/pop/peek
 */
public class InvestigationTrail {

    /**
     * Stack storing the navigation history.
     * Each entry is a view name such as "PROFILE:2001" or "CONNECTION_GRAPH".
     */
    private final Stack<String> history = new Stack<>();

    /** The view currently being displayed. */
    private String currentView = "MAIN_MENU";

    /** How many levels deep into an investigation chain the investigator has gone. */
    private int trailDepth = 0;

    /**
     * Pushes the current view onto the Stack, then moves to the destination.
     * Increments trailDepth to track how deep the chain is.
     *
     * @param destination the name of the view to navigate to
     */
    public void navigateTo(String destination) {
        history.push(currentView);
        currentView = destination;
        trailDepth++;
        System.out.println("-> Navigated to: " + destination + "  (depth: " + trailDepth + ")");
    }

    /**
     * Pops the Stack to return to the previous view.
     * Does nothing if history is empty.
     *
     * @return the view returned to
     */
    public String goBack() {
        if (history.isEmpty()) {
            System.out.println("Already at the start of the trail.");
            return currentView;
        }
        String previous = history.pop();
        currentView = previous;
        trailDepth  = Math.max(0, trailDepth - 1);
        System.out.println("<- Returned to: " + currentView);
        return currentView;
    }

    /**
     * Returns the top of the Stack without removing it.
     * Used to preview the previous location without navigating back.
     *
     * @return the previous view name, or "No previous location." if empty
     */
    public String peekPrevious() {
        if (history.isEmpty()) {
            return "No previous location.";
        }
        return history.peek();
    }

    /**
     * Clones and reverses the Stack to print the full navigation path
     * in chronological order from first step to current view.
     */
    public void viewTrail() {
        System.out.println("═".repeat(50));
        System.out.println("  INVESTIGATION TRAIL (depth: " + trailDepth + ")");
        System.out.println("═".repeat(50));
        if (history.isEmpty()) {
            System.out.println("  No trail recorded yet.");
        } else {
            // Clone so we don't destroy the real history
            Stack<String> copy     = (Stack<String>) history.clone();
            Stack<String> reversed = new Stack<>();
            while (!copy.isEmpty()) {
                reversed.push(copy.pop());
            }
            int step = 1;
            while (!reversed.isEmpty()) {
                System.out.println("  " + step + ". " + reversed.pop());
                step++;
            }
        }
        System.out.println("  -> Current: " + currentView);
        System.out.println("═".repeat(50));
    }

    /**
     * Empties the Stack and resets to MAIN_MENU for a fresh investigation session.
     */
    public void clearTrail() {
        history.clear();
        currentView = "MAIN_MENU";
        trailDepth  = 0;
        System.out.println("Investigation trail cleared.");
    }

    /**
     * Returns the name of the view currently being displayed.
     *
     * @return current view name
     */
    public String getCurrentView() {
        return currentView;
    }

    /**
     * Returns how many levels deep the investigator has navigated.
     *
     * @return trail depth
     */
    public int getTrailDepth() {
        return trailDepth;
    }
}