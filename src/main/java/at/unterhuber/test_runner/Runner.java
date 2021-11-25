package at.unterhuber.test_runner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class Runner {
    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double threshold = 0.8;
        String path = args[0];
        RiskCalculator calculator = new RiskCalculator(path, new RiskMetric[]{new RiskMetric("Cbo"), new RiskMetric("NumberOfMethods")});
        FileClassLoader loader = new FileClassLoader(path);
        TestSelector selector = new TestSelector(loader, threshold);
        TestExecutor executor = new TestExecutor(selector);

        loader.initClassLoader();
        loader.loadClasses();

        calculator.measure();
        calculator.initRiskMeasurements();
        calculator.printSelectedMetrics();
        HashMap<String, Double> risk = calculator.getRiskByClass();

        selector.determineClassesToTest(risk);

        executor.executeTests();
    }
}
