package at.unterhuber.test_runner.git;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Apriori {
    private final List<Set<Integer>> transactions;
    private final int numItems;
    private final int numTransactions;
    private final double minSupport;
    private final double minConfidence;

    private final List<Set<Integer>> frequentItemSets = new ArrayList<>();
    private List<Set<Integer>> itemSets;

    public Apriori(List<Set<Integer>> transactions, double minSupport, double minConfidence) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.transactions = transactions;
        this.numItems = transactions
                .stream()
                .map((transaction) -> transaction.stream().max(Integer::compareTo).orElse(Integer.MIN_VALUE))
                .max(Integer::compareTo)
                .orElse(Integer.MIN_VALUE);
        this.numTransactions = transactions.size();
    }

    private static Set<Set<Integer>> allSubsetsOf(Set<Integer> set) {
        Integer[] array = set.toArray(new Integer[0]);
        Set<Set<Integer>> subsets = new HashSet<>();
        int n = array.length;
        for (int i = 0; i < (1 << n); i++) {
            Set<Integer> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0)
                    subset.add(array[j]);
            }
            subsets.add(subset);
        }
        return subsets;
    }

    public List<Combination<Integer>> find() {
        createItemSetsOfSize1();
        while (itemSets.size() > 0) {
            calculateFrequentItemSets();
            if (itemSets.size() != 0) {
                createNewItemSetsFromPreviousOnes();
            }
        }
        return filterByConfidence();
    }

    private List<Combination<Integer>> filterByConfidence() {
        List<Combination<Integer>> combinations = new ArrayList<>();
        for (Set<Integer> itemSet : frequentItemSets) {
            for (Set<Integer> leftSet : allSubsetsOf(itemSet)) {
                Set<Integer> rightSet = new HashSet<>(itemSet);
                rightSet.removeAll(leftSet);
                if (leftSet.size() == 0 || rightSet.size() == 0) continue;
                int matches = 0, total = 0;
                for (Set<Integer> transaction : transactions) {
                    if (transaction.containsAll(leftSet)) {
                        ++total;
                        if (transaction.containsAll(rightSet)) {
                            ++matches;
                        }
                    }
                }
                double confidence = (double) matches / total;
                if (confidence >= minConfidence) {
                    combinations.add(new Combination<>(leftSet, rightSet, confidence));
                }
            }
        }
        return combinations;
    }

    private void createItemSetsOfSize1() {
        itemSets = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            itemSets.add(Set.of(i));
        }
    }

    private void createNewItemSetsFromPreviousOnes() {
        Set<Set<Integer>> tempCandidates = new HashSet<>();

        for (int i = 0; i < itemSets.size(); i++) {
            for (int j = i + 1; j < itemSets.size(); j++) {
                Set<Integer> set1 = itemSets.get(i);
                Set<Integer> set2 = new HashSet<>(itemSets.get(j));
                Set<Integer> newCandidate = new HashSet<>(set1);
                set2.removeAll(newCandidate);
                if (set2.size() == 1) {
                    newCandidate.addAll(set2);
                    tempCandidates.add(newCandidate);
                }
            }
        }

        itemSets = new ArrayList<>(tempCandidates);
    }

    private void calculateFrequentItemSets() {
        int[] counts = new int[itemSets.size()];

        for (Set<Integer> transaction : transactions) {
            for (int i = 0; i < itemSets.size(); i++) {
                Set<Integer> itemSet = itemSets.get(i);
                if (transaction.containsAll(itemSet)) {
                    counts[i]++;
                }
            }
        }

        itemSets = IntStream.range(0, itemSets.size())
                .filter((i) -> (counts[i] / (double) (numTransactions)) >= minSupport)
                .mapToObj(itemSets::get)
                .collect(Collectors.toList());
        frequentItemSets.addAll(itemSets);
    }

    public static class Combination<T> {
        private final Set<T> leftSet;
        private final Set<T> rightSet;
        private final double confidence;

        public Combination(Set<T> leftSet, Set<T> rightSet, double confidence) {
            this.leftSet = leftSet;
            this.rightSet = rightSet;
            this.confidence = confidence;
        }

        public <U> Combination<U> mapWith(Map<T, U> map) {
            return new Combination<>(
                    leftSet.stream().map(map::get).collect(Collectors.toSet()),
                    rightSet.stream().map(map::get).collect(Collectors.toSet()),
                    confidence
            );
        }

        public Set<T> getLeftSet() {
            return leftSet;
        }

        public Set<T> getRightSet() {
            return rightSet;
        }

        @Override
        public String toString() {
            return leftSet + " ==> " + rightSet;
        }
    }
}
