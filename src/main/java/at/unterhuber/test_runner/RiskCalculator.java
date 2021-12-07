package at.unterhuber.test_runner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RiskCalculator {
    private final MetricMeasure measure;
    private final Config config;

    public RiskCalculator(MetricMeasure measure, Config config) {
        this.measure = measure;
        this.config = config;
    }

    public HashMap<String, Double> getRiskByClass() {
        HashMap<String, Double> risk = new HashMap<>();
        for (List<Measurement> measurements : measure.getMeasurements().values()) {
            for (Measurement measurement : measurements) {
                double value = risk.getOrDefault(measurement.getClassName(), 0.0);
                System.out.println(measurement);
                risk.put(measurement.getClassName(), value + measurement.getRelativeValue() * config.getWeightOf(measurement.getMetric()));
            }
        }
        System.out.println(Arrays.toString(risk.values().toArray()));
        return risk;
    }
}
