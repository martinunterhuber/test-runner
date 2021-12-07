package at.unterhuber.test_runner;

import java.util.*;


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
        String path = args[0];
        String[] changedFiles = System.getenv("DIFF").split(" ");
        RiskMetric[] riskMetrics = Arrays.stream(metricNames).map(RiskMetric::new).toArray(RiskMetric[]::new);
        RiskMetric[] testRiskMetrics = Arrays.stream(testMetricNames).map(RiskMetric::new).toArray(RiskMetric[]::new);

        ProjectPathHandler pathHandler = new GradlePathHandler(path);
        Config config = new Config(pathHandler.getRootPath());
        FileClassLoader loader = new FileClassLoader(pathHandler);
        TestSelector selector = new TestSelector(loader, changedFiles, config);
        TestExecutor executor = new TestExecutor(selector);
        MetricMeasure measure = new MetricMeasure(pathHandler.getMainSourcePath().toString(), riskMetrics);
        MetricMeasure testMeasure = new MetricMeasure(pathHandler.getTestSourcePath().toString(), testRiskMetrics);
        RiskCalculator calculator = new RiskCalculator(measure, config);
        RiskCalculator testCalculator = new RiskCalculator(testMeasure, config);
        IssueMeasure issueMeasure = new IssueMeasure(pathHandler);

        config.loadConfig();

        loader.initClassLoader();
        loader.loadTestClasses();

        measure.measure();
        measure.initRiskMeasurements();
        measure.printSelectedMetrics();

        testMeasure.measure();
        testMeasure.initRiskMeasurements();
        testMeasure.printSelectedMetrics();

        HashMap<String, Double> risk = calculator.getRiskByClass();
        HashMap<String, Double> testRisk = testCalculator.getRiskByClass();

        issueMeasure.initIssuesByClass();
        Map<String, List<Issue>> issues = issueMeasure.getIssues();
        Map<String, List<Issue>> testIssues = issueMeasure.getTestIssues();

        selector.determineClassesToTest(risk, issues);
        selector.determineTestsToRun(testRisk, testIssues);

        executor.executeTests();
    }
}
