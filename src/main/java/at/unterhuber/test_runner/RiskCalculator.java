package at.unterhuber.test_runner;

import java.util.HashMap;
import java.util.List;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

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
                risk.put(measurement.getClassName(), value + measurement.getRelativeValue() * config.getWeightOf(measurement.getMetric()));
            }
        }
        System.out.println("Metrics\n" + toLineSeparatedString(risk) + "\n");
        return risk;
    }
}
