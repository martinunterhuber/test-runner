package at.unterhuber.test_runner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Runner {
    private static final String[] metricNames = new String[]{
            "Wmc",
            "Rfc",
            "Cbo",
            "Dit",
            "NumberOfMethods",
            "NumberOfFields"
    };

    private static final double threshold = 0.4;

    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String path = args[0];
        String changed = args[1];
        String[] changedFiles = changed.split(" ");
        System.out.println(Arrays.toString(changedFiles));

        ProjectPathHandler pathHandler = new GradlePathHandler(path);
        FileClassLoader loader = new FileClassLoader(pathHandler);
        TestSelector selector = new TestSelector(loader, threshold);
        TestExecutor executor = new TestExecutor(selector);
        LimitConfig config = new LimitConfig(pathHandler.getRootPath(), metricNames);
        RiskCalculator calculator = new RiskCalculator(
                path,
                Arrays.stream(metricNames).map(RiskMetric::new).toArray(RiskMetric[]::new),
                config
        );

        config.loadConfig();

        loader.initClassLoader();
        loader.loadTestClasses();

        calculator.measure();
        calculator.initRiskMeasurements();
        calculator.printSelectedMetrics();
        HashMap<String, Double> risk = calculator.getRiskByClass();

        selector.determineClassesToTest(risk, changedFiles);

        executor.executeTests();
    }
}
