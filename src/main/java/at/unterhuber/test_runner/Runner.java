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
    private static final double metricThreshold = 3.0;
    private static final double testMetricThreshold = 1.5;
    private static final int issueThreshold = 20;

    public static void main(String[] args) throws Throwable {
        String path = args[0];
        String[] changedFiles = System.getenv("DIFF").split(" ");
        RiskMetric[] riskMetrics = Arrays.stream(metricNames).map(RiskMetric::new).toArray(RiskMetric[]::new);
        RiskMetric[] testRiskMetrics = Arrays.stream(testMetricNames).map(RiskMetric::new).toArray(RiskMetric[]::new);

        ProjectPathHandler pathHandler = new GradlePathHandler(path);
        FileClassLoader loader = new FileClassLoader(pathHandler);
        TestSelector selector = new TestSelector(loader, metricThreshold, testMetricThreshold, issueThreshold);
        TestExecutor executor = new TestExecutor(selector);
        Config config = new Config(pathHandler.getRootPath(), metricNames);
        MetricMeasure measure = new MetricMeasure(path + "src/main/", riskMetrics);
        MetricMeasure testMeasure = new MetricMeasure(path + "src/test/", testRiskMetrics);
        RiskCalculator calculator = new RiskCalculator(measure, config);
        RiskCalculator testCalculator = new RiskCalculator(testMeasure, config);
        MyPMD myPMD = new MyPMD(pathHandler);

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

        Map<String, List<Issue>> issue = myPMD.getIssuesByClass();

        selector.determineClassesToTest(risk, testRisk, changedFiles, issue);

        executor.executeTests();
    }
}
