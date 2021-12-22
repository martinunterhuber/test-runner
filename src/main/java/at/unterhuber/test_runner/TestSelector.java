package at.unterhuber.test_runner;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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

    public void determineClassesToTest(HashMap<String, Double> risk, Map<String, List<Issue>> issues) {
        classesToTest.addAll(getClassesToTestByMetric(risk));
        classesToTest.addAll(getClassesToTestByIssues(issues));
        if (changeSet != null) {
            excludeUnchanged();
        }
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
                .map(Path::of)
                .map(loader::getFullClassNameFrom)
                .collect(Collectors.toSet());
    }

    private void excludeUnchanged() {
        classesToTest = classesToTest
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toSet());
    }

    public void determineTestsToRun(HashMap<String, Double> testRisk, Map<String, List<Issue>> testIssues) {
        testClassesToRun.addAll(getTestClassesToRunByMetric(testRisk));
        testClassesToRun.addAll(getTestClassesToRunByIssues(testIssues));
        if (changeSet != null) {
            excludeUnchangedTests();
        }
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
                .map(Path::of)
                .map(loader::getFullClassNameFrom)
                .collect(Collectors.toSet());
    }

    private void excludeUnchangedTests() {
        testClassesToRun = testClassesToRun
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toSet());
    }

    public List<DiscoverySelector> selectTestClasses() throws IOException {
        System.out.println("Risky classes: " + classesToTest);
        System.out.println("Risky tests: " + testClassesToRun);

        Set<DiscoverySelector> selectors = resolver
                .resolveDependenciesFor(new ArrayList<>(classesToTest))
                .stream()
                .filter(loader::isTestClass)
                .map(DiscoverySelectors::selectClass)
                .collect(Collectors.toSet());

        selectors.addAll(
                testClassesToRun
                        .stream()
                        .map(DiscoverySelectors::selectClass)
                        .collect(Collectors.toSet())
        );
        System.out.println("Selected tests: " + selectors);
        return new ArrayList<>(selectors);
    }
}
