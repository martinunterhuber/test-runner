package at.unterhuber.test_runner;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestSelector {
    private final FileClassLoader loader;
    private final Set<String> changeSet;
    private final Config config;

    private Set<String> classesToTest = new HashSet<>();
    private Set<String> testClassesToRun = new HashSet<>();

    public TestSelector(FileClassLoader loader, String[] changedFiles, Config config) {
        this.loader = loader;
        this.changeSet = getChangeSet(changedFiles);
        this.config = config;
    }

    private Set<String> getChangeSet(String[] changedFiles) {
        return Arrays
                .stream(changedFiles)
                .map(file -> loader.getFullClassNameFrom(Path.of(file)))
                .collect(Collectors.toSet());
    }

    public void determineClassesToTest(HashMap<String, Double> risk, Map<String, List<Issue>> issues) {
        classesToTest.addAll(getClassesToTestByMetric(risk));
        classesToTest.addAll(getClassesToTestByIssues(issues));
        // excludeUnchanged();
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

    private void excludeUnchanged(String[] changedFiles) {
        classesToTest = classesToTest
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toSet());
    }

    public void determineTestsToRun(HashMap<String, Double> testRisk, Map<String, List<Issue>> testIssues) {
        testClassesToRun.addAll(getTestClassesToRunByMetric(testRisk));
        testClassesToRun.addAll(getTestClassesToRunByIssues(testIssues));
        // excludeUnchangedTests();
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

    private void excludeUnchangedTests(String[] changedFiles) {
        testClassesToRun = testClassesToRun
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toSet());
    }

    public List<DiscoverySelector> selectTestClasses() {
        System.out.println(Arrays.toString(classesToTest.toArray()));
        System.out.println(Arrays.toString(testClassesToRun.toArray()));
        List<Field> fields = loader.getTestFields();
        Set<DiscoverySelector> selectors = fields.stream()
                .filter(field -> classesToTest.contains(field.getType().getCanonicalName()))
                .map(field -> selectClass(field.getDeclaringClass()))
                .collect(Collectors.toSet());
        selectors.addAll(
                testClassesToRun
                        .stream()
                        .map(DiscoverySelectors::selectClass)
                        .collect(Collectors.toSet())
        );
        System.out.println(Arrays.toString(selectors.toArray()));
        return new ArrayList<>(selectors);
    }
}
