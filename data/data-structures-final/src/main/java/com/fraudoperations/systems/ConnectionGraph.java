package com.fraudoperations.systems;

import com.fraudoperations.models.Suspect;

import java.time.LocalDate;
import java.util.*;

/**
 * Feature 7 - Connection Graph
 * Builds and queries a network of suspects linked by shared identifiers.
 * Identifiers are cross-referenced using the four hash index maps from HashIndexSystem.
 *
 * Data structures used:
 *   HashMap<Integer, List<Connection>> - adjacency list for the graph
 *   LinkedHashSet<Integer>             - ordered cluster sets during BFS
 *   LinkedList (Queue)                 - BFS traversal queue in detectClusters()
 *   ArrayList<Connection>              - per-node connection lists
 */
public class ConnectionGraph {

    /**
     * Represents a single edge in the connection graph between two suspects.
     * Custom inner class storing the two suspect IDs, connection type,
     * shared value, and detection date.
     */
    public static class Connection {

        /** ID of the first suspect in the connection. */
        public int suspectID_A;

        /** ID of the second suspect in the connection. */
        public int suspectID_B;

        /** The type of shared identifier: IP, EMAIL, ADDRESS, or NAME. */
        public String connectionType;

        /** The actual value the two suspects share. */
        public String sharedValue;

        /** The date this connection was first detected. */
        public String dateDetected;

        /**
         * Constructs a Connection between two suspects.
         *
         * @param a    suspectID of the first suspect
         * @param b    suspectID of the second suspect
         * @param type connection type (IP, EMAIL, ADDRESS, NAME)
         * @param value the shared identifier value
         * @param date  date detected (YYYY-MM-DD)
         */
        public Connection(int a, int b, String type, String value, String date) {
            this.suspectID_A    = a;
            this.suspectID_B    = b;
            this.connectionType = type;
            this.sharedValue    = value;
            this.dateDetected   = date;
        }

        /**
         * Returns a formatted edge description for console display.
         *
         * @return formatted connection string
         */
        @Override
        public String toString() {
            return String.format("  [%d] <-> [%d]  via %s (\"%s\")  detected: %s",
                    suspectID_A, suspectID_B, connectionType, sharedValue, dateDetected);
        }
    }

    // Fields
    /** Adjacency list: suspectID to list of Connection objects. Backed by HashMap. */
    private final Map<Integer, List<Connection>> adjacencyList = new HashMap<>();

    /** Reference to the hash index system for identifier lookups. */
    private final HashIndexSystem hashIndex;

    /** Reference to the master suspect database. */
    private final Map<Integer, Suspect> suspectDB;

    /**
     * Constructs a ConnectionGraph with references to the hash index and suspect database.
     *
     * @param hashIndex the shared hash index system
     * @param suspectDB the master suspect Map
     */
    public ConnectionGraph(HashIndexSystem hashIndex, Map<Integer, Suspect> suspectDB) {
        this.hashIndex = hashIndex;
        this.suspectDB = suspectDB;
    }

    /**
     * Iterates all four index maps and links any suspects sharing the same hash key.
     * Must be called after all suspects are indexed.
     * Also mirrors connections onto each Suspect's connections ArrayList.
     */
    public void buildConnectionGraph() {
        adjacencyList.clear();
        String today = LocalDate.now().toString();

        buildFromIndex(hashIndex.getIpIndex(),      "IP",      today);
        buildFromIndex(hashIndex.getEmailIndex(),   "EMAIL",   today);
        buildFromIndex(hashIndex.getAddressIndex(), "ADDRESS", today);
        buildFromIndex(hashIndex.getNameIndex(),    "NAME",    today);

        // Mirror connections onto Suspect objects
        for (Map.Entry<Integer, List<Connection>> entry : adjacencyList.entrySet()) {
            Suspect fraudster = suspectDB.get(entry.getKey());
            if (fraudster == null) {
                continue;
            }
            for (Connection c : entry.getValue()) {
                int otherId;
                if (c.suspectID_A == entry.getKey()) {
                    otherId = c.suspectID_B;
                } else {
                    otherId = c.suspectID_A;
                }
                fraudster.addConnection(otherId);
            }
        }

        System.out.println("Connection graph built. Total nodes with connections: "
                + adjacencyList.size());
    }

    /**
     * Iterates one index map and creates Connection objects for every pair of
     * suspects that share the same hash bucket (meaning they share that identifier).
     *
     * @param index          one of the four hash index maps
     * @param connectionType IP, EMAIL, ADDRESS, or NAME
     * @param date           today's date for the dateDetected field
     */
    private void buildFromIndex(Map<Integer, List<Integer>> index,
                                String connectionType, String date) {
        for (Map.Entry<Integer, List<Integer>> entry : index.entrySet()) {
            List<Integer> ids = entry.getValue();
            if (ids.size() < 2) {
                continue;
            }
            // Every pair in the bucket shares this hash key
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    int a = ids.get(i);
                    int b = ids.get(j);
                    String sharedValue = resolveSharedValue(a, b, connectionType);
                    Connection conn = new Connection(a, b, connectionType, sharedValue, date);

                    List<Connection> listA = adjacencyList.get(a);
                    if (listA == null) {
                        listA = new ArrayList<>();
                        adjacencyList.put(a, listA);
                    }
                    listA.add(conn);

                    List<Connection> listB = adjacencyList.get(b);
                    if (listB == null) {
                        listB = new ArrayList<>();
                        adjacencyList.put(b, listB);
                    }
                    listB.add(conn);
                }
            }
        }
    }

    /**
     * Looks up the shared value for a given connection type from suspect A's record.
     *
     * @param idA  first suspect ID
     * @param idB  second suspect ID (unused — value comes from A's record)
     * @param type IP, EMAIL, ADDRESS, or NAME
     * @return the shared value string, or "unknown" if suspect not found
     */
    private String resolveSharedValue(int idA, int idB, String type) {
        Suspect a = suspectDB.get(idA);
        if (a == null) {
            return "unknown";
        }
        switch (type) {
            case "IP":
                return a.getIpAddress();
            case "EMAIL":
                return a.getEmail();
            case "ADDRESS":
                return a.getAddress();
            case "NAME":
                return a.getName();
            default:
                return "unknown";
        }
    }

    /**
     * Returns all Connection objects for a given suspect from the adjacency list.
     *
     * @param suspectID the suspect to look up
     * @return list of Connections, or an empty list if no connections found
     */
    public List<Connection> getConnectionsForSuspect(int suspectID) {
        List<Connection> result = adjacencyList.get(suspectID);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    /**
     * Given any IP, email, address, or name value, returns every suspect ID
     * associated with it via the appropriate index map.
     *
     * @param value the identifier value to search for
     * @param type  IP, EMAIL, ADDRESS, or NAME
     * @return list of suspect IDs sharing this value
     */
    public List<Integer> findSharedIdentifier(String value, String type) {
        switch (type.toUpperCase()) {
            case "IP":
                return hashIndex.lookupByIP(value);
            case "EMAIL":
                return hashIndex.lookupByEmail(value);
            case "ADDRESS":
                return hashIndex.lookupByAddress(value);
            case "NAME":
                return hashIndex.lookupByName(value);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Counts how many data points (connections of any type) two suspects share.
     *
     * @param idA first suspect ID
     * @param idB second suspect ID
     * @return number of shared identifiers between them
     */
    public int getConnectionStrength(int idA, int idB) {
        List<Connection> connections = adjacencyList.get(idA);
        if (connections == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            if ((c.suspectID_A == idA && c.suspectID_B == idB)
                    || (c.suspectID_A == idB && c.suspectID_B == idA)) {
                count++;
            }
        }
        return count;
    }

    /**
     * BFS to find all groups of mutually connected suspects (fraud rings).
     * Uses a LinkedList-backed Queue for the BFS traversal.
     *
     * @return list of sets, where each set is one connected cluster
     */
    public List<Set<Integer>> detectClusters() {
        Set<Integer> visited       = new HashSet<>();
        List<Set<Integer>> clusters = new ArrayList<>();

        // Visit every suspect in the graph
        for (Integer startID : adjacencyList.keySet()) {
            // Skip already explored suspects
            if (visited.contains(startID)) {
                continue;
            }

            // Current fraud ring being built
            Set<Integer> cluster = new LinkedHashSet<>();
            // BFS queue backed by LinkedList
            Queue<Integer> queue = new LinkedList<>();
            queue.add(startID);

            // Breadth-First Search
            while (!queue.isEmpty()) {
                int current = queue.poll();

                // Skip if already processed
                if (visited.contains(current)) {
                    continue;
                }
                visited.add(current);
                cluster.add(current);

                List<Connection> connections = adjacencyList.get(current);
                if (connections == null) {
                    continue;
                }

                // Explore neighbors
                for (int i = 0; i < connections.size(); i++) {
                    Connection c = connections.get(i);
                    int neighbor;
                    if (c.suspectID_A == current) {
                        neighbor = c.suspectID_B;
                    } else {
                        neighbor = c.suspectID_A;
                    }
                    // Queue unexplored neighbors
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }

            // Ignore isolated suspects — only record groups of 2+
            if (cluster.size() > 1) {
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    /**
     * Prints the full connection graph with labeled edges and identified fraud rings.
     */
    public void displayConnectionGraph() {
        System.out.println("═".repeat(70));
        System.out.println("  CONNECTION GRAPH");
        System.out.println("═".repeat(70));

        for (Map.Entry<Integer, List<Connection>> entry : adjacencyList.entrySet()) {
            Suspect fraudster = suspectDB.get(entry.getKey());
            String label;
            if (fraudster != null) {
                label = fraudster.getName();
            } else {
                label = "Unknown";
            }

            System.out.printf("%n  Node [%d] %s:%n", entry.getKey(), label);
            Set<String> printed = new HashSet<>();
            List<Connection> connections = entry.getValue();

            for (int i = 0; i < connections.size(); i++) {
                Connection c = connections.get(i);
                String key = Math.min(c.suspectID_A, c.suspectID_B) + "-"
                        + Math.max(c.suspectID_A, c.suspectID_B) + "-"
                        + c.connectionType;
                if (printed.add(key)) {
                    System.out.println(c);
                }
            }
        }

        System.out.println();
        List<Set<Integer>> clusters = detectClusters();
        System.out.println("  DETECTED FRAUD RINGS (" + clusters.size() + "):");

        for (int i = 0; i < clusters.size(); i++) {
            Set<Integer> cluster = clusters.get(i);
            List<String> names = new ArrayList<>();
            for (int id : cluster) {
                Suspect fraudster = suspectDB.get(id);
                String displayName;
                if (fraudster != null) {
                    displayName = id + " (" + fraudster.getName() + ")";
                } else {
                    displayName = String.valueOf(id);
                }
                names.add(displayName);
            }
            System.out.println("  Ring " + (i + 1) + ": " + String.join(" - ", names));
        }

        System.out.println("═".repeat(70));
    }
}