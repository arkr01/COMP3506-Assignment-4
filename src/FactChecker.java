import java.util.*;

public class FactChecker {

    /**
     * Represents an event (e.g. person A arrived).
     */
    private static class Event {
        /**
         * Represents if a person arrived or left.
         */
        private enum EventType {
            ARRIVED,
            LEFT
        }

        /** The name of the person who has arrived/left */
        private String name;
        
        /** Whether the person arrived or left */
        private EventType arrivedOrLeft;

        /**
         * Constructs a new vertex in the graph.
         *
         * @param arrivedOrLeft whether the person arrived/left
         * @param name name of the person
         */
        private Event(EventType arrivedOrLeft, String name) {
            this.arrivedOrLeft = arrivedOrLeft;
            this.name = name;
        }

        /**
         * Checks whether two Vertices are equal (i.e. they have the same parameters)
         *
         * @param toCompare (Ideally) another vertex to compare with
         * @return true if vertices are equal, false otherwise
         */
        @Override
        public boolean equals(Object toCompare) {
            if (this == toCompare) {
                return true;
            }
            if (toCompare == null || this.getClass() != toCompare.getClass()) {
                return false;
            }
            Event otherEvent = (Event) toCompare;
            return this.arrivedOrLeft == otherEvent.arrivedOrLeft &&
                    this.name.equals(otherEvent.name);
        }

        /**
         * Calculates the hashCode for this Event.
         *
         * @return hashCode for this Event
         */
        @Override
        public int hashCode() {
            return Objects.hash(name, arrivedOrLeft);
        }
    }

    /**
     * Checks if a list of facts is internally consistent. 
     * That is, can they all hold true at the same time?
     * Or are two (or potentially more) facts logically incompatible?
     * 
     * @param facts list of facts to check consistency of
     * @return true if all the facts are internally consistent, otherwise false.
     */
    public static boolean areFactsConsistent(List<Fact> facts) {
        // Construct directed graph in which vertices denote whether a person has arrived/left,
        // and (directed) edges denote chronology.
        HashMap<Event, HashSet<Event>> graph = constructGraph(facts);

        // As (directed) edges denote chronology of events, a consistent set of facts would lead
        // to a DAG. Check (and return) if this is indeed the case.
        return !hasCycles(graph);
    }

    /**
     * Constructs a directed graph from the given list of facts.
     *
     * @param facts the list of facts to process.
     * @return a graph representation of said facts
     */
    private static HashMap<Event, HashSet<Event>> constructGraph(List<Fact> facts) {
        HashMap<Event, HashSet<Event>> graph = new HashMap<>();

        for (Fact fact : facts) {
            Event personAArrived = new Event(Event.EventType.ARRIVED, fact.getPersonA());
            Event personALeft = new Event(Event.EventType.LEFT, fact.getPersonA());

            Event personBArrived = new Event(Event.EventType.ARRIVED, fact.getPersonB());
            Event personBLeft = new Event(Event.EventType.LEFT, fact.getPersonB());

            // Add events (vertices)
            if (!graph.containsKey(personAArrived)) {
                graph.put(personAArrived, new HashSet<>(Set.of(personALeft)));
            }
            if (!graph.containsKey(personALeft)) {
                graph.put(personALeft, new HashSet<>());
            }
            if (!graph.containsKey(personBArrived)) {
                graph.put(personBArrived, new HashSet<>(Set.of(personBLeft)));
            }
            if (!graph.containsKey(personBLeft)) {
                graph.put(personBLeft, new HashSet<>());
            }

            // Add directed edges
            if (fact.getType().equals(Fact.FactType.TYPE_ONE)) {
                graph.get(personALeft).add(personBArrived);
            } else {
                graph.get(personAArrived).add(personBLeft);
                graph.get(personBArrived).add(personALeft);
            }
        }
        return graph;
    }

    /**
     * Checks (via DFS) and returns if the graph has any cycles.
     *
     * @param graph the graph to check
     * @return true if the graph has a cycle, false otherwise.
     */
    private static boolean hasCycles(HashMap<Event, HashSet<Event>> graph) {
        ArrayList<Event> occurred = new ArrayList<>();

        // current path to check (represents stack)
        LinkedList<Event> currentPath = new LinkedList<>();

        // Perform DFS
        for (Event current : graph.keySet()) {
            if (checkPath(graph, current, occurred, currentPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks each path from a starting event (i.e. vertex) for cycles (via DFS), and returns
     * whether any are found.
     *
     * @param graph the graph to check
     * @param current the current event to search for cycles from
     * @param occurred the occurred events (i.e. visited vertices)
     * @param currentPath the current series of events to traverse (acts as a stack)
     * @return true if a cycle is found, false otherwise
     */
    private static boolean checkPath(HashMap<Event, HashSet<Event>> graph, Event current,
            ArrayList<Event> occurred, LinkedList<Event> currentPath) {
        // We have found a back edge ==> A cycle
        if (currentPath.contains(current)) {
            return true;
        }

        // If this event has already occurred, but it is not in the current path, then we have
        // not found a cycle. Instead, we have simply performed a search from a different event
        // (vertex), that has resulted in identifying an event that we have already examined.
        // There can therefore be no cycle from this point onwards.
        if (occurred.contains(current)) {
            return false;
        }
        occurred.add(current);
        currentPath.addFirst(current); // represents push

        for (Event next : graph.get(current)) {
            if (checkPath(graph, next, occurred, currentPath)) {
                return true;
            }
        }
        currentPath.removeFirst(); // represents pop
        return false;
    }
}