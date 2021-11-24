package at.unterhuber.test_runner;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKNotifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class RiskCalculator {
    private final RiskMetric[] metrics;
    private final Map<String, CKClassResult> ckMeasurements;
    private final Map<String, List<RiskMeasurement>> measurements;
    private final String path;

    public RiskCalculator(String path, RiskMetric[] metrics) {
        this.path = path;
        this.metrics = metrics;
        this.ckMeasurements = new HashMap<>();
        this.measurements = new HashMap<>();
    }

    public void measure() {
        new CK().calculate(path + "src/main/", new CKNotifier() {
            @Override
            public void notify(CKClassResult result) {
                ckMeasurements.put(result.getClassName(), result);
            }

            @Override
            public void notifyError(String sourceFilePath, Exception e) {
                System.err.println("Error in " + sourceFilePath);
                e.printStackTrace(System.err);
            }
        });
    }

    public void initRiskMeasurements() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (RiskMetric metric : metrics) {
            List<RiskMeasurement> metricMeasurements = new ArrayList<>();
            measurements.put(metric.name, metricMeasurements);
            int max = 0;
            for (CKClassResult result : ckMeasurements.values()) {
                Method method = CKClassResult.class.getMethod("get" + metric.name);
                int value = (int) method.invoke(result);
                metricMeasurements.add(new RiskMeasurement(result.getClassName(), metric, value));
                if (value > max) {
                    max = value;
                }
            }

            for (RiskMeasurement measurement : metricMeasurements) {
                measurement.forMaxValue(max);
            }
        }
    }

    public HashMap<String, Double> getRiskByClass() {
        HashMap<String, Double> risk = new HashMap<>();
        for (List<RiskMeasurement> measurements : this.measurements.values()) {
            for (RiskMeasurement measurement : measurements) {
                double value = risk.getOrDefault(measurement.getClassName(), 0.0);
                System.out.println(measurement.getRelativeValue());
                risk.put(measurement.getClassName(), value + measurement.getRelativeValue() / this.measurements.size());
            }
        }
        System.out.println(Arrays.toString(risk.values().toArray()));
        return risk;
    }

    public void printSelectedMetrics() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for(Map.Entry<String, CKClassResult> entry : ckMeasurements.entrySet()) {
            for (RiskMetric metric : metrics) {
                Method method = CKClassResult.class.getMethod("get" + metric.name);
                System.out.println(entry.getKey() + " - " + metric.name + ": " + method.invoke(entry.getValue()));
            }
        }
    }
}
