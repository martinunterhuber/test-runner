package at.unterhuber.test_runner.test;

import at.unterhuber.test_runner.bug.Bug;
import at.unterhuber.test_runner.dependency.DependencyResolver;
import at.unterhuber.test_runner.issue.Issue;
import at.unterhuber.test_runner.util.Config;
import at.unterhuber.test_runner.util.FileClassLoader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class TestSelector {
    private final FileClassLoader loader;
    private final Config config;
    private final DependencyResolver resolver;
    private Set<String> changeSet;
    private Set<String> classesToTest = new HashSet<>();
    private Set<String> testClassesToRun = new HashSet<>();

    public TestSelector(FileClassLoader loader, Config config, DependencyResolver resolver) {
        this.loader = loader;
        this.changeSet = null;
        this.config = config;
        this.resolver = resolver;
    }

    public void determineChangeSet(List<String> changedFiles) throws IOException {
        changeSet = resolver.resolveDependenciesFor(changedFiles);
    }

    public void determineClassesToTest(HashMap<String, Double> risk, Map<String, List<Issue>> issues, Map<String, List<Bug>> bugs) {
        classesToTest.addAll(getClassesToTestByMetric(risk));
        classesToTest.addAll(getClassesToTestByIssues(issues));
        classesToTest.addAll(getClassesToTestByBugs(bugs));
        if (changeSet != null) {
            excludeUnchanged();
        }
    }

    private Collection<String> getClassesToTestByBugs(Map<String, List<Bug>> bugs) {
        return bugs.entrySet()
                .stream()
                .filter(entry -> entry
                        .getValue()
                        .stream()
                        .map(Bug::computeRisk)
                        .reduce(Integer::sum)
                        .orElse(0) > config.getBugThreshold())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<String> getClassesToTestByMetric(HashMap<String, Double> risk) {
        return risk.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > config.getMetricThreshold())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<String> getClassesToTestByIssues(Map<String, List<Issue>> issues) {
        return issues.entrySet()
                .stream()
                .filter(entry -> entry
                        .getValue()
                        .stream()
                        .map(Issue::computeRisk)
                        .reduce(Integer::sum)
                        .orElse(0) > config.getIssueThreshold())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void excludeUnchanged() {
        classesToTest = classesToTest
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toSet());
    }

    public void determineTestsToRun(HashMap<String, Double> testRisk, Map<String, List<Issue>> testIssues, Map<String, List<Bug>> testBugs) {
        testClassesToRun.addAll(getTestClassesToRunByMetric(testRisk));
        testClassesToRun.addAll(getTestClassesToRunByIssues(testIssues));
        testClassesToRun.addAll(getTestClassesToRunByBugs(testBugs));
        if (changeSet != null) {
            excludeUnchangedTests();
        }
    }

    private Collection<String> getTestClassesToRunByBugs(Map<String, List<Bug>> bugs) {
        return bugs.entrySet()
                .stream()
                .filter(entry -> entry
                        .getValue()
                        .stream()
                        .map(Bug::computeRisk)
                        .reduce(Integer::sum)
                        .orElse(0) > config.getTestBugThreshold())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<String> getTestClassesToRunByMetric(HashMap<String, Double> risk) {
        return risk.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > config.getTestMetricThreshold())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<String> getTestClassesToRunByIssues(Map<String, List<Issue>> issues) {
        return issues.entrySet()
                .stream()
                .filter(entry -> entry
                        .getValue()
                        .stream()
                        .map(Issue::computeRisk)
                        .reduce(Integer::sum)
                        .orElse(0) > config.getTestIssueThreshold())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void excludeUnchangedTests() {
        testClassesToRun = testClassesToRun
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toSet());
    }

    public List<String> selectTestClasses() throws IOException {
        System.out.println("Risky classes\n" + toLineSeparatedString(classesToTest) + "\n");
        System.out.println("Risky tests\n" + toLineSeparatedString(testClassesToRun) + "\n");

        Set<String> selectors = resolver
                .resolveDependenciesFor(new ArrayList<>(classesToTest))
                .stream()
                .filter(loader::isTestClass)
                .collect(Collectors.toSet());

        selectors.addAll(testClassesToRun);
        System.out.println("Selected tests\n" + toLineSeparatedString(selectors) + "\n");
        return new ArrayList<>(selectors);
    }

    public Set<String> getClassesToTest() {
        return classesToTest;
    }

    public Set<String> getTestClassesToRun() {
        return testClassesToRun;
    }
}
