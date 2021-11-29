package at.unterhuber.test_runner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;


public class Runner {
    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double threshold = 0.4;
        String path = args[0];
        String changed = args[1];
        String[] changedFiles = changed.split(" ");
        System.out.println(Arrays.toString(changedFiles));
        RiskCalculator calculator = new RiskCalculator(
                path, new RiskMetric[]{
                        new RiskMetric("Wmc"),
                        new RiskMetric("Rfc"),
                        new RiskMetric("Cbo"),
                        new RiskMetric("Dit"),
                        new RiskMetric("NumberOfMethods"),
                }
        );
        ProjectPathHandler pathHandler = new GradlePathHandler(path);
        FileClassLoader loader = new FileClassLoader(pathHandler);
        TestSelector selector = new TestSelector(loader, threshold);
        TestExecutor executor = new TestExecutor(selector);

        loader.initClassLoader();
        loader.loadClasses();

        calculator.measure();
        calculator.initRiskMeasurements();
        calculator.printSelectedMetrics();
        HashMap<String, Double> risk = calculator.getRiskByClass();

        selector.determineClassesToTest(risk, changedFiles);

        executor.executeTests();
    }
}
