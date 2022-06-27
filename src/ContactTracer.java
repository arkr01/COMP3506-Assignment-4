import java.util.*;

public class ContactTracer {

    /** Represents the entire tracing system. The key denotes a person, and the value HashMap has
     *  other people who have traces with said person as keys, and has the timestamps as the
     *  values. */
    private HashMap<String, HashMap<String, HashSet<Integer>>> tracingSystem;

    /**
     * Represents a contagious person.
     */
    private static class ContagiousPerson {
        /** The person's name */
        private String name;

        /** When the person became contagious */
        private int timeOfContagion;

        /**
         * Constructs a new contagious person.
         *
         * @param name the person's name
         * @param timeOfContagion the time the person became contagious (in minutes)
         */
        private ContagiousPerson(String name, int timeOfContagion) {
            this.name = name;
            this.timeOfContagion = timeOfContagion;
        }
    }

    /** Represents an hour of time passed. The exact time since a person coming into contact with
     * the disease may become contagious.
     */
    private static final int HOUR = 60;

    /**
     * Initialises an empty ContactTracer with no populated contact traces.
     */
    public ContactTracer() {
        this.tracingSystem = new HashMap<>();
    }

    /**
     * Initialises the ContactTracer and populates the internal data structures
     * with the given list of contract traces.
     * 
     * @param traces to populate with
     * @require traces != null
     */
    public ContactTracer(List<Trace> traces) {
        this.tracingSystem = new HashMap<>();
        for (Trace current : traces) {
            this.addTrace(current);
        }
    }

    /**
     * Adds a new contact trace to 
     * 
     * If a contact trace involving the same two people at the exact same time is
     * already stored, do nothing.
     * 
     * @param trace to add
     * @require trace != null
     */
    public void addTrace(Trace trace) {
        if (!this.tracingSystem.containsKey(trace.getPerson1())) {
            this.tracingSystem.put(trace.getPerson1(), new HashMap<>());
        }
        if (!this.tracingSystem.get(trace.getPerson1()).containsKey(trace.getPerson2())) {
            this.tracingSystem.get(trace.getPerson1()).put(trace.getPerson2(), new HashSet<>());
        }

        // Traces are undirected, system must ensure to maintain this
        if (!this.tracingSystem.containsKey(trace.getPerson2())) {
            this.tracingSystem.put(trace.getPerson2(), new HashMap<>());
        }
        if (!this.tracingSystem.get(trace.getPerson2()).containsKey(trace.getPerson1())) {
            this.tracingSystem.get(trace.getPerson2()).put(trace.getPerson1(), new HashSet<>());
        }

        // HashSet and HashMap prevent duplicate values and keys respectively, so if contact trace
        // with same people at same time exists, nothing will be done.
        this.tracingSystem.get(trace.getPerson1()).get(trace.getPerson2()).add(trace.getTime());
        this.tracingSystem.get(trace.getPerson2()).get(trace.getPerson1()).add(trace.getTime());
    }

    /**
     * Gets a list of times that person1 and person2 have come into direct 
     * contact (as per the tracing data).
     *
     * If the two people haven't come into contact before, an empty list is returned.
     * 
     * Otherwise the list should be sorted in ascending order.
     * 
     * @param person1 
     * @param person2
     * @return a list of contact times, in ascending order.
     * @require person1 != null && person2 != null
     */
    public List<Integer> getContactTimes(String person1, String person2) {
        ArrayList<Integer> contactTimes = new ArrayList<>();
        if (this.tracingSystem.get(person1).containsKey(person2)) {
            contactTimes.addAll(this.tracingSystem.get(person1).get(person2));
            Collections.sort(contactTimes);
        }
        return contactTimes;
    }

    /**
     * Gets all the people that the given person has been in direct contact with
     * over the entire history of the tracing dataset.
     * 
     * @param person to list direct contacts of
     * @return set of the person's direct contacts
     */
    public Set<String> getContacts(String person) {
        return this.tracingSystem.get(person).keySet();
    }

    /**
     * Gets all the people that the given person has been in direct contact with
     * at OR after the given timestamp (i.e. inclusive).
     * 
     * @param person to list direct contacts of
     * @param timestamp to filter contacts being at or after
     * @return set of the person's direct contacts at or after the timestamp
     */
    public Set<String> getContactsAfter(String person, int timestamp) {
        HashSet<String> contactsAfter = new HashSet<>();
        for (String otherPerson : this.getContacts(person)) {
            // Just need to compare the most recent direct contact time (this.getContactTimes()
            // is sorted in ascending order so this is just the last element of the list)
            List<Integer> contactTimes = this.getContactTimes(person, otherPerson);
            if (contactTimes.get(contactTimes.size() - 1) >= timestamp) {
                contactsAfter.add(otherPerson);
            }
        }
        return contactsAfter;
    }

    /**
     * Initiates a contact trace starting with the given person, who
     * became contagious at timeOfContagion.
     * 
     * Note that the return set shouldn't include the original person the trace started from.
     * 
     * @param person to start contact tracing from
     * @param timeOfContagion the exact time person became contagious
     * @return set of people who may have contracted the disease, originating from person
     */
    public Set<String> contactTrace(String person, int timeOfContagion) {
        ArrayList<String> visitedPeople = new ArrayList<>();

        // represents the queue for BFS
        LinkedList<ContagiousPerson> contagiousPeople = new LinkedList<>();

        // Final set of people to be tested for the disease
        HashSet<String> toBeTested = new HashSet<>();

        ContagiousPerson source = new ContagiousPerson(person, timeOfContagion);
        contagiousPeople.addLast(source); // enqueue
        visitedPeople.add(person);

        // Perform BFS to identify people to be tested
        while (!contagiousPeople.isEmpty()) {
            ContagiousPerson currentInfected = contagiousPeople.removeFirst(); // dequeue

            for (String potentialInfected : this.tracingSystem.get(currentInfected.name).keySet()) {
                if (!visitedPeople.contains(potentialInfected)) {
                    // Find earliest interaction that was an hour after currentInfected became
                    // contagious
                    for (Integer time : this.getContactTimes(currentInfected.name,
                            potentialInfected)) {
                        // the potentially infected is now potentially contagious
                        if (time - currentInfected.timeOfContagion >= HOUR) {
                            ContagiousPerson infected = new ContagiousPerson(potentialInfected,
                                    time + HOUR);

                            // enqueue to check people this person may infect/have infected
                            contagiousPeople.addLast(infected);
                            toBeTested.add(potentialInfected);
                            break;
                        }
                    }
                    visitedPeople.add(potentialInfected);
                }
            }
        }
        return toBeTested;
    }
}
