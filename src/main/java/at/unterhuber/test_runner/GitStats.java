package at.unterhuber.test_runner;

import java.util.*;
import java.util.stream.Collectors;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class GitStats {
    private final List<GitCommit> commits;
    private final Map<Integer, String> reverseIdMap;
    private Map<String, Date> lastModified;
    private Map<String, Date> created;
    private Map<String, Integer> numChanges;
    private Map<String, Set<String>> contributors;

    public GitStats(List<GitCommit> commits, Map<Integer, String> reverseIdMap) {
        this.commits = commits;
        this.reverseIdMap = reverseIdMap;
    }

    public void initStats() {
        lastModified = new HashMap<>();
        created = new HashMap<>();
        numChanges = new HashMap<>();
        contributors = new HashMap<>();

        for (GitCommit commit : commits) {
            for (GitFileChange fileChange : commit.getChanges()) {
                String clazz = fileChange.getClazz();
                if (!lastModified.containsKey(clazz)) {
                    lastModified.put(clazz, commit.getDate());
                }
                created.put(clazz, commit.getDate());
                numChanges.put(clazz, numChanges.getOrDefault(clazz, 0) + 1);
                if (!contributors.containsKey(clazz)) {
                    contributors.put(clazz, new HashSet<>());
                }
                contributors.get(clazz).add(commit.getAuthor());
            }
        }
    }

    public Date getOldestDate() {
        return lastModified
                .values()
                .stream()
                .min(Date::compareTo)
                .orElse(new Date(0L));
    }

    public Date lastModificationOf(String clazz) {
        return lastModified.getOrDefault(clazz, new Date(0L));
    }

    public Date creationOf(String clazz) {
        return created.getOrDefault(clazz, new Date(0L));
    }

    public int getMaxChanges() {
        return numChanges
                .values()
                .stream()
                .max(Integer::compareTo)
                .orElse(1);
    }

    public int changeCountOf(String clazz) {
        return numChanges.getOrDefault(clazz, 0);
    }

    public int getMaxContributors() {
        return contributors
                .values()
                .stream()
                .map(Set::size)
                .max(Integer::compareTo)
                .orElse(1);
    }

    public Set<String> contributorsOf(String clazz) {
        return contributors.getOrDefault(clazz, new HashSet<>());
    }

    public int contributorCountOf(String clazz) {
        return contributors.getOrDefault(clazz, new HashSet<>()).size();
    }

    public List<Apriori.Combination<String>> findFilesOftenChangedTogether() {
        List<Set<Integer>> transactions = new ArrayList<>();
        for (GitCommit commit : commits) {
            if (commit.getChanges().isEmpty()) {
                continue;
            }
            Set<Integer> set = commit
                    .getChanges()
                    .stream()
                    .map(GitFileChange::getId)
                    .collect(Collectors.toSet());
            transactions.add(set);
        }
        double minSupport = Math.sqrt(1d / transactions.size());
        List<Apriori.Combination<Integer>> idCombinations = null;
        while (idCombinations == null || idCombinations.size() < 5) {
            Apriori apriori = new Apriori(transactions, minSupport, 0.8);
            idCombinations = apriori.find();
            minSupport /= 1.5;
        }
        List<Apriori.Combination<String>> combinations = idCombinations
                .stream()
                .map((combination) -> combination.mapWith(reverseIdMap))
                .collect(Collectors.toList());
        System.out.println("Files often changed together");
        System.out.println(toLineSeparatedString(combinations));
        System.out.println();
        return combinations;
    }
}
