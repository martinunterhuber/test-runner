package at.unterhuber.test_runner;

import java.util.*;

public class RiskCalculator {
    private final MetricMeasure measure;
    private final LimitConfig config;

    public RiskCalculator(MetricMeasure measure, LimitConfig config) {
        this.measure = measure;
        this.config = config;
    }

    public HashMap<String, Double> getRiskByClass() {
        HashMap<String, Double> risk = new HashMap<>();
        for (List<RiskMeasurement> measurements : measure.getMeasurements().values()) {
            for (RiskMeasurement measurement : measurements) {
                double value = risk.getOrDefault(measurement.getClassName(), 0.0);
                System.out.println(measurement);
                if (measurement.getValue() > config.getLimitOf(measurement.getMetric())) {
                    risk.put(measurement.getClassName(), 1d);
                }
                risk.put(measurement.getClassName(), value + measurement.getRelativeValue() / measure.getMeasurements().size());
            }
        }
        System.out.println(Arrays.toString(risk.values().toArray()));
        return risk;
    }
}
