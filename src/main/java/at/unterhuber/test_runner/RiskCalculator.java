package at.unterhuber.test_runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class RiskCalculator {
    private final MetricMeasure measure;
    private final Config config;

    private HashMap<String, Double> risks;
    private List<Apriori.Combination<String>> combinations = new ArrayList<>();

    private static final double propagationFactor = 0.5;
    private GitStats stats;

    public RiskCalculator(MetricMeasure measure, Config config) {
        this.measure = measure;
        this.config = config;
    }

    public HashMap<String, Double> getRiskByClass() {
        risks = new HashMap<>();
        for (List<Measurement> measurements : measure.getMeasurements().values()) {
            for (Measurement measurement : measurements) {
                double value = risks.getOrDefault(measurement.getClassName(), 0.0);
                risks.put(measurement.getClassName(), value + measurement.getRelativeValue() * config.getWeightOf(measurement.getMetric()));
            }
        }
        System.out.println("Metrics\n" + toLineSeparatedString(risks) + "\n");
        shareRiskOftenChangedTogether();
        return risks;
    }

    public void shareRiskOftenChangedTogether() {
        HashMap<String, Double> temp = new HashMap<>(risks);
        for (Apriori.Combination<String> combination : combinations) {
            double riskSum = 0d;
            int count = combination.getLeftSet().size();
            for (String left : combination.getLeftSet()) {
                Double risk = temp.get(left);
                if (risk != null) {
                    riskSum += risk;
                }
            }
            double averageRisk = riskSum / count;
            for (String right : combination.getRightSet()) {
                Double risk = risks.get(right);
                if (averageRisk > risk) {
                    risk = (risk + averageRisk * propagationFactor) / (1 + propagationFactor);
                    risks.put(right, risk);
                }
            }
        }
        System.out.println("Metrics (with sharing risk)\n" + toLineSeparatedString(risks) + "\n");
    }

    public void setCombinations(List<Apriori.Combination<String>> combinations) {
        this.combinations = combinations;
    }

    public void setStats(GitStats stats) {
        this.stats = stats;
    }
}
