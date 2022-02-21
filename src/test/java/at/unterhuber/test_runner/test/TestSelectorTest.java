package at.unterhuber.test_runner.test;

import at.unterhuber.test_runner.bug.Bug;
import at.unterhuber.test_runner.dependency.DependencyResolver;
import at.unterhuber.test_runner.dependency.DummyDependencyResolver;
import at.unterhuber.test_runner.issue.Issue;
import at.unterhuber.test_runner.util.Config;
import at.unterhuber.test_runner.util.DummyClassLoader;
import at.unterhuber.test_runner.util.FileClassLoader;
import net.sourceforge.pmd.RulePriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestSelectorTest {
    private static Config config;
    private static FileClassLoader loader;
    private static DependencyResolver resolver;

    private TestSelector selector;

    private HashMap<String, Double> risk;
    private Map<String, List<Issue>> issues;
    private Map<String, List<Bug>> bugs;

    @BeforeAll
    public static void setupAll() {
        config = new Config(Path.of("src/test/resources"), Path.of(""));
        config.loadConfigFromFile();
        loader = new DummyClassLoader();
        resolver = new DummyDependencyResolver();
    }

    @BeforeEach
    public void setup() {
        selector = new TestSelector(loader, config, resolver);
        risk = new HashMap<>();
        issues = new HashMap<>();
        bugs = new HashMap<>();
    }

    @Test
    public void testDetermineClassesToTest_ByRisk() {
        risk.put("at.unterhuber.test.MyClass", 2.5);
        risk.put("at.unterhuber.test.MyOtherClass", 3.1);

        selector.determineClassesToTest(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyOtherClass"), selector.getClassesToTest());
    }

    @Test
    public void testDetermineClassesToTest_ByIssues() {
        issues.put("at.unterhuber.test.MyClass", List.of(new Issue(RulePriority.MEDIUM), new Issue(RulePriority.HIGH)));
        issues.put("at.unterhuber.test.MyOtherClass", List.of(new Issue(RulePriority.MEDIUM)));

        selector.determineClassesToTest(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyClass"), selector.getClassesToTest());
    }

    @Test
    public void testDetermineClassesToTest_ByBugs() {
        bugs.put("at.unterhuber.test.MyClass", List.of(new Bug(12), new Bug(20)));
        bugs.put("at.unterhuber.test.MyOtherClass", List.of(new Bug(19)));

        selector.determineClassesToTest(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyClass"), selector.getClassesToTest());
    }

    @Test
    public void testDetermineClassesToTest_Mixed() {
        risk.put("at.unterhuber.test.MyClass", 2.5);
        risk.put("at.unterhuber.test.MyOtherClass", 3.1);
        risk.put("at.unterhuber.test.Cls", 1.1);
        issues.put("at.unterhuber.test.MyClass", List.of(new Issue(RulePriority.MEDIUM), new Issue(RulePriority.HIGH)));
        issues.put("at.unterhuber.test.MyOtherClass", List.of(new Issue(RulePriority.MEDIUM)));
        issues.put("at.unterhuber.test.Cls", List.of(new Issue(RulePriority.LOW)));
        bugs.put("at.unterhuber.test.MyClass", List.of(new Bug(12), new Bug(20)));
        bugs.put("at.unterhuber.test.MyOtherClass", List.of(new Bug(19)));
        bugs.put("at.unterhuber.test.Cls", List.of());

        selector.determineClassesToTest(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyClass", "at.unterhuber.test.MyOtherClass"), selector.getClassesToTest());
    }

    @Test
    public void testDetermineTestsToRun_ByRisk() {
        risk.put("at.unterhuber.test.MyTestClass", 1.1);
        risk.put("at.unterhuber.test.MyOtherTestClass", 3.1);

        selector.determineTestsToRun(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyOtherTestClass"), selector.getTestClassesToRun());
    }

    @Test
    public void testDetermineTestsToRun_ByIssues() {
        issues.put("at.unterhuber.test.MyTestClass", List.of(new Issue(RulePriority.MEDIUM), new Issue(RulePriority.HIGH)));
        issues.put("at.unterhuber.test.MyOtherTestClass", List.of(new Issue(RulePriority.MEDIUM)));

        selector.determineTestsToRun(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyTestClass"), selector.getTestClassesToRun());
    }

    @Test
    public void testDetermineTestsToRun_ByBugs() {
        bugs.put("at.unterhuber.test.MyTestClass", List.of(new Bug(12), new Bug(20)));
        bugs.put("at.unterhuber.test.MyOtherTestClass", List.of(new Bug(19)));

        selector.determineTestsToRun(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyTestClass"), selector.getTestClassesToRun());
    }

    @Test
    public void testDetermineTestsToRun_Mixed() {
        risk.put("at.unterhuber.test.MyTestClass", 1.1);
        risk.put("at.unterhuber.test.MyOtherTestClass", 3.1);
        risk.put("at.unterhuber.test.TestClass", 1.3);
        issues.put("at.unterhuber.test.MyTestClass", List.of(new Issue(RulePriority.MEDIUM), new Issue(RulePriority.HIGH)));
        issues.put("at.unterhuber.test.MyOtherTestClass", List.of(new Issue(RulePriority.MEDIUM)));
        issues.put("at.unterhuber.test.TestClass", List.of(new Issue(RulePriority.LOW)));
        bugs.put("at.unterhuber.test.MyTestClass", List.of(new Bug(12), new Bug(20)));
        bugs.put("at.unterhuber.test.MyOtherTestClass", List.of(new Bug(19)));
        bugs.put("at.unterhuber.test.TestClass", List.of());

        selector.determineTestsToRun(risk, issues, bugs);

        Assertions.assertEquals(Set.of("at.unterhuber.test.MyTestClass", "at.unterhuber.test.MyOtherTestClass"), selector.getTestClassesToRun());
    }

    @Test
    public void testSelectTestClasses() throws IOException {
        HashMap<String, Double> testRisk = new HashMap<>();
        Map<String, List<Issue>>  testIssues = new HashMap<>();
        Map<String, List<Bug>> testBugs = new HashMap<>();

        risk.put("at.unterhuber.test.MyClass", 2.5);
        risk.put("at.unterhuber.test.MyOtherClass", 3.1);
        risk.put("at.unterhuber.test.Cls", 1.1);
        issues.put("at.unterhuber.test.MyClass", List.of(new Issue(RulePriority.MEDIUM), new Issue(RulePriority.HIGH)));
        issues.put("at.unterhuber.test.MyOtherClass", List.of(new Issue(RulePriority.MEDIUM)));
        issues.put("at.unterhuber.test.Cls", List.of(new Issue(RulePriority.LOW)));
        bugs.put("at.unterhuber.test.MyClass", List.of(new Bug(12), new Bug(20)));
        bugs.put("at.unterhuber.test.MyOtherClass", List.of(new Bug(19)));
        bugs.put("at.unterhuber.test.Cls", List.of());

        testRisk.put("at.unterhuber.test.MyTestClass", 1.1);
        testRisk.put("at.unterhuber.test.MyOtherTestClass", 3.1);
        testRisk.put("at.unterhuber.test.TestClass", 1.3);
        testIssues.put("at.unterhuber.test.MyTestClass", List.of(new Issue(RulePriority.MEDIUM)));
        testIssues.put("at.unterhuber.test.MyOtherTestClass", List.of(new Issue(RulePriority.MEDIUM)));
        testIssues.put("at.unterhuber.test.TestClass", List.of(new Issue(RulePriority.LOW)));
        testBugs.put("at.unterhuber.test.MyTestClass", List.of(new Bug(20), new Bug(20)));
        testBugs.put("at.unterhuber.test.MyOtherTestClass", List.of(new Bug(14)));
        testBugs.put("at.unterhuber.test.TestClass", List.of());

        selector.determineClassesToTest(risk, issues, bugs);
        selector.determineTestsToRun(testRisk, testIssues, testBugs);
        List<DiscoverySelector> discoverySelectors = selector.selectTestClasses();

        Assertions.assertEquals(List.of(selectClass("at.unterhuber.test.MyTestClass"), selectClass("at.unterhuber.test.MyOtherTestClass")), discoverySelectors);
    }
}
