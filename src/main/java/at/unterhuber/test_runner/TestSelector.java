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
    private final double metricThreshold;
    private final int issueThreshold;
    private final double testMetricThreshold;
    private List<String> classesToTest = new ArrayList<>();
    private List<String> testClassesToRun = new ArrayList<>();

    public TestSelector(FileClassLoader loader, double metricThreshold, double testMetricThreshold, int issueThreshold) {
        this.loader = loader;
        this.metricThreshold = metricThreshold;
        this.issueThreshold = issueThreshold;
        this.testMetricThreshold = testMetricThreshold;
    }

    public void determineClassesToTest(HashMap<String, Double> risk, HashMap<String, Double> testRisk, String[] changedFiles, Map<String, List<SonarIssue>> issues) {
        Set<String> classesToTest1 = getClassesToTestByMetric(risk);
        Set<String> classesToTest2 = getClassesToTestByIssues(issues);
        testClassesToRun.addAll(getTestClassesToRun(testRisk));
        classesToTest.addAll(classesToTest2);
        classesToTest.addAll(classesToTest1);
        // excludeUnchanged(changedFiles);
    }

    private void excludeUnchanged(String[] changedFiles) {
        Set<String> changeSet = getChangeSet(changedFiles);
        classesToTest = classesToTest
                .stream()
                .filter(changeSet::contains)
                .collect(Collectors.toList());
    }

    private Set<String> getClassesToTestByIssues(Map<String, List<SonarIssue>> issues) {
        return issues.entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream().map(SonarIssue::computeRisk).reduce(Integer::sum).orElse(0) > issueThreshold)
                .map(Map.Entry::getKey)
                .map(Path::of)
                .map(loader::getFullClassNameFrom)
                .collect(Collectors.toSet());
    }

    private Set<String> getTestClassesToRun(HashMap<String, Double> risk) {
        return risk.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > testMetricThreshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<String> getClassesToTestByMetric(HashMap<String, Double> risk) {
        return risk.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > metricThreshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<String> getChangeSet(String[] changedFiles) {
        return Arrays
                .stream(changedFiles)
                .map(file -> loader.getFullClassNameFrom(Path.of(file)))
                .collect(Collectors.toSet());
    }

    public List<DiscoverySelector> selectTestClasses() {
        System.out.println(Arrays.toString(classesToTest.toArray()));
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
        return selectors.stream().toList();
    }
}
