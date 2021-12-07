package at.unterhuber.test_runner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Runner {
    // TODO: add these to config?
    private static final String[] metricNames = new String[]{
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
    private static final double metricThreshold = 0.4;
    private static final int issueThreshold = 20;
    private static final String projectName = "martinunterhuber_test-project";

    public static void main(String[] args) throws Throwable {
        String path = args[0];
        String[] changedFiles = System.getenv("DIFF").split(" ");
        RiskMetric[] riskMetrics = Arrays.stream(metricNames).map(RiskMetric::new).toArray(RiskMetric[]::new);
        RiskMetric[] testRiskMetrics = Arrays.stream(testMetricNames).map(RiskMetric::new).toArray(RiskMetric[]::new);

        SonarIssueParser issueParser = new SonarIssueParser(projectName);
        ProjectPathHandler pathHandler = new GradlePathHandler(path);
        FileClassLoader loader = new FileClassLoader(pathHandler);
        TestSelector selector = new TestSelector(loader, metricThreshold, issueThreshold);
        TestExecutor executor = new TestExecutor(selector);
        LimitConfig config = new LimitConfig(pathHandler.getRootPath(), metricNames);
        MetricMeasure measure = new MetricMeasure(path + "src/main/", riskMetrics);
        MetricMeasure testMeasure = new MetricMeasure(path + "src/test/", testRiskMetrics);
        RiskCalculator calculator = new RiskCalculator(measure, config);
        RiskCalculator testCalculator = new RiskCalculator(testMeasure, config);

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

        Map<String, List<SonarIssue>> issues = issueParser.getIssuesByClass();

        selector.determineClassesToTest(risk, changedFiles, issues);

        executor.executeTests();
    }
}
