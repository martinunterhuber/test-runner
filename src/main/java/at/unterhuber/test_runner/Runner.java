package at.unterhuber.test_runner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Runner {
    // TODO: add these to config?
    private static final String[] metricNames = new String[]{
            "Loc",
            "Wmc",
            "Rfc",
            "Cbo",
            "Dit",
            "NumberOfMethods",
            "NumberOfFields"
    };
    private static final String[] testMetricNames = new String[]{
            "Wmc",
            "NumberOfMethods"
    };

    public static void main(String[] args) throws Throwable {
        String rootPath = args[0];

        Metric[] metrics = Arrays.stream(metricNames).map(Metric::new).toArray(Metric[]::new);
        Metric[] testMetrics = Arrays.stream(testMetricNames).map(Metric::new).toArray(Metric[]::new);

        ProjectPathHandler pathHandler = new GradlePathHandler(rootPath);
        Config config = new Config(pathHandler.getRootPath());
        FileClassLoader loader = new FileClassLoader(pathHandler);
        DependencyResolver resolver = new DependencyResolver(loader, pathHandler);
        TestSelector selector = new TestSelector(loader, config, resolver);
        TestExecutor executor = new TestExecutor(selector);
        MetricMeasure measure = new MetricMeasure(pathHandler.getMainSourcePath().toString(), metrics);
        MetricMeasure testMeasure = new MetricMeasure(pathHandler.getTestSourcePath().toString(), testMetrics);
        RiskCalculator calculator = new RiskCalculator(measure, config);
        RiskCalculator testCalculator = new RiskCalculator(testMeasure, config);
        IssueMeasure issueMeasure = new IssueMeasure(pathHandler);

        List<String> changedFiles = Arrays
                .stream(System.getenv("DIFF").split(" "))
                .map(pathHandler::pathToFullClassName)
                .collect(Collectors.toList());
        System.out.println("Changed files: " + changedFiles);

        config.loadConfig();

        loader.initClassLoader();
        loader.loadTestClasses();

        measure.measure();
        measure.initMeasurements();
        // measure.printSelectedMetrics();

        testMeasure.measure();
        testMeasure.initMeasurements();
        // testMeasure.printSelectedMetrics();

        HashMap<String, Double> risk = calculator.getRiskByClass();
        HashMap<String, Double> testRisk = testCalculator.getRiskByClass();

        issueMeasure.initIssuesByClass();
        Map<String, List<Issue>> issues = issueMeasure.getIssues();
        Map<String, List<Issue>> testIssues = issueMeasure.getTestIssues();

        selector.determineChangeSet(changedFiles);
        selector.determineClassesToTest(risk, issues);
        selector.determineTestsToRun(testRisk, testIssues);

        executor.executeTests();
    }
}
