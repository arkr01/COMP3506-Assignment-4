import java.util.*;

public class ErdosNumbers {
    /**
     * String representing Paul Erdos's name to check against
     */
    public static final String ERDOS = "Paul Erdös";

    /** Represents a key-value pair between an author and the papers they have contributed to. */
    private HashMap<String, HashSet<String>> authorPapers;

    /** Represents the (weighted) graph via an adjacency list, where the weight is the number of
     * papers between the author and the collaborator. */
    private HashMap<String, HashMap<String, Integer>> authorCoAuthors;

    /** Represents a key-value pair between an author and their Erdos number. */
    private HashMap<String, Integer> authorErdosNumbers;

    /** Represents a key-value pair between an author and their weighted Erdos number. */
    private HashMap<String, WeightedErdosDistance> authorWeightedErdosNumbers;

    /**
     * Represents the current weighted edge distances between a given author and Erdos. For use
     * within a PriorityQueue.
     */
    private static class WeightedErdosDistance implements Comparable<WeightedErdosDistance> {
        /** Represents the current weighted distance from this author and Erdos. */
        private Double weightedErdosDistance;

        /** The name of this author */
        private String name;

        /**
         * Constructs a new author/weighted erdos distance pair.
         */
        private WeightedErdosDistance(String name) {
            this.name = name;

            // Initialise everyone (excluding Erdos) as an 'infinite distance' away from Erdos
            this.weightedErdosDistance = (name.equals(ERDOS)) ? 0.0 : Double.MAX_VALUE;
        }

        /**
         * Updates the weighted edge distance between this author and Erdos.
         *
         * @param newWeightedErdosDistance the new weighted edge distance
         */
        private void updateWeightedErdosDistance(Double newWeightedErdosDistance) {
            this.weightedErdosDistance = newWeightedErdosDistance;
        }

        /**
         * Compares weighted distances between this author and collaborator, used to define
         * PriorityQueue.removeMin().
         *
         * @param collaborator the collaborator to compare weighted distances with
         * @return 1 if this author's weighted distance is larger, -1 if collaborator's weighted
         * distance is larger, 0 if equal
         */
        @Override
        public int compareTo(WeightedErdosDistance collaborator) {
            return this.weightedErdosDistance.compareTo(collaborator.weightedErdosDistance);
        }
    }

    /**
     * Initialises the class with a list of papers and authors.
     *
     * Each element in 'papers' corresponds to a String of the form:
     * 
     * [paper name]:[author1][|author2[|...]]]
     *
     * Note that for this constructor and the below methods, authors and papers
     * are unique (i.e. there can't be multiple authors or papers with the exact same name or
     * title).
     * 
     * @param papers List of papers and their authors
     */
    public ErdosNumbers(List<String> papers) {
        this.authorPapers = new HashMap<>();
        this.authorCoAuthors = new HashMap<>();
        this.authorErdosNumbers = new HashMap<>();
        this.authorWeightedErdosNumbers = new HashMap<>();

        for (String paper : papers) {
            String[] paperAndAuthors = paper.split(":");

            String[] authors = paperAndAuthors[1].split("\\|");
            for (String author : authors) {
                initialiseAuthorPairs(author, authors);

                // Add the papers that each author has contributed to
                this.authorPapers.get(author).add(paperAndAuthors[0]);
            }
        }
        findErdosNumbers();
        findWeightedErdosNumbers();
    }

    /**
     * Provides an initial author representation for all the data structures.
     *
     * @param author the author to be represented
     */
    private void initialiseAuthorPairs(String author, String[] authors) {
        if (!this.authorPapers.containsKey(author)) {
            this.authorPapers.put(author, new HashSet<>());
        }
        if (!this.authorCoAuthors.containsKey(author)) {
            this.authorCoAuthors.put(author, new HashMap<>()); // add new vertex
        }
        for (String collaborator : authors) {
            if (!collaborator.equals(author)) {
                if (!this.authorCoAuthors.get(author).containsKey(collaborator)) {
                    this.authorCoAuthors.get(author).put(collaborator, 0); // add new edge
                }
                // update edge weight for each new collaboration
                this.authorCoAuthors.get(author).put(collaborator,
                        this.authorCoAuthors.get(author).get(collaborator) + 1);
            }
        }

        // Initialise Erdos and Weighted Erdos Numbers
        int initialErdosNumber = (author.equals(ERDOS)) ? 0 : Integer.MAX_VALUE;
        if (!this.authorErdosNumbers.containsKey(author)) {
            this.authorErdosNumbers.put(author, initialErdosNumber);
        }
        if (!this.authorWeightedErdosNumbers.containsKey(author)) {
            this.authorWeightedErdosNumbers.put(author, new WeightedErdosDistance(author));
        }
    }

    /**
     * Calculates the Erdos numbers for each author using BFS.
     */
    private void findErdosNumbers() {
        ArrayList<String> visitedAuthors = new ArrayList<>();

        LinkedList<String> authors = new LinkedList<>(); // represents the queue
        authors.addLast(ERDOS); // represents enqueue
        visitedAuthors.add(ERDOS);

        while (!authors.isEmpty()) {
            String currentAuthor = authors.removeFirst(); // represents dequeue

            for (String collaborator : this.authorCoAuthors.get(currentAuthor).keySet()) {
                if (!visitedAuthors.contains(collaborator)) {
                    authors.addLast(collaborator);

                    this.authorErdosNumbers.put(collaborator,
                            this.authorErdosNumbers.get(currentAuthor) + 1);

                    visitedAuthors.add(collaborator);
                }
            }
        }
    }

    /**
     * Calculates the weighted Erdos numbers for each author using Dijkstra's Algorithm.
     */
    private void findWeightedErdosNumbers() {
        PriorityQueue<WeightedErdosDistance> distances = new PriorityQueue<>();
        
        for (Map.Entry<String, WeightedErdosDistance> authorWeightedDistance :
                this.authorWeightedErdosNumbers.entrySet()) {
            distances.add(authorWeightedDistance.getValue());
        }

        while (!distances.isEmpty()) {
            WeightedErdosDistance currentAuthor = distances.remove(); // represents PQ.removeMin()
            for (Map.Entry<String, Integer> collaborator :
                    this.authorCoAuthors.get(currentAuthor.name).entrySet()) {

                double relaxingDistance = currentAuthor.weightedErdosDistance +
                        1.0 / collaborator.getValue(); // weight is 1/numCollaborations

                WeightedErdosDistance collaboratorWeightedDistance =
                        this.authorWeightedErdosNumbers.get(collaborator.getKey());

                // Perform edge relaxation
                if (relaxingDistance < collaboratorWeightedDistance.weightedErdosDistance) {
                    distances.remove(collaboratorWeightedDistance);
                    collaboratorWeightedDistance.updateWeightedErdosDistance(relaxingDistance);
                    distances.add(collaboratorWeightedDistance);
                }
            }
        }
    }
    
    /**
     * Gets all the unique papers the author has written (either solely or
     * as a co-author).
     * 
     * @param author to get the papers for.
     * @return the unique set of papers this author has written.
     */
    public Set<String> getPapers(String author) {
        return this.authorPapers.get(author);
    }

    /**
     * Gets all the unique co-authors the author has written a paper with.
     *
     * @param author to get collaborators for
     * @return the unique co-authors the author has written with.
     */
    public Set<String> getCollaborators(String author) {
        return this.authorCoAuthors.get(author).keySet();
    }

    /**
     * Checks if Erdos is connected to all other author's given as input to
     * the class constructor.
     * 
     * In other words, does every author in the dataset have an Erdos number?
     * 
     * @return the connectivity of Erdos to all other authors.
     */
    public boolean isErdosConnectedToAll() {
        return !this.authorErdosNumbers.containsValue(Integer.MAX_VALUE);
    }

    /**
     * Calculate the Erdos number of an author. 
     * 
     * This is defined as the length of the shortest path on a graph of paper 
     * collaborations (as explained in the assignment specification).
     * 
     * If the author isn't connected to Erdos (and in other words, doesn't have
     * a defined Erdos number), returns Integer.MAX_VALUE.
     * 
     * Note: Erdos himself has an Erdos number of 0.
     * 
     * @param author to calculate the Erdos number of
     * @return authors' Erdos number or otherwise Integer.MAX_VALUE
     */
    public int calculateErdosNumber(String author) {
        return this.authorErdosNumbers.get(author);
    }

    /**
     * Gets the average Erdos number of all the authors on a paper.
     * If a paper has just a single author, this is just the author's Erdos number.
     *
     * Note: Erdos himself has an Erdos number of 0.
     *
     * @param paper to calculate it for
     * @return average Erdos number of paper's authors
     */
    public double averageErdosNumber(String paper) {
        int total = 0;
        HashSet<String> authorsOfPaper = new HashSet<>();
        for (Map.Entry<String, HashSet<String>> authorPaperPair : this.authorPapers.entrySet()) {
            if (authorPaperPair.getValue().contains(paper)) {
                authorsOfPaper.add(authorPaperPair.getKey());
            }
        }

        for (String author : authorsOfPaper) {
            total += this.calculateErdosNumber(author);
        }
        return (double) total / authorsOfPaper.size();
    }

    /**
     * Calculates the "weighted Erdos number" of an author.
     * 
     * If the author isn't connected to Erdos (and in other words, doesn't have
     * an Erdos number), returns Double.MAX_VALUE.
     *
     * Note: Erdos himself has a weighted Erdos number of 0.
     * 
     * @param author to calculate it for
     * @return author's weighted Erdos number
     */
    public double calculateWeightedErdosNumber(String author) {
        return this.authorWeightedErdosNumbers.get(author).weightedErdosDistance;
    }
}
