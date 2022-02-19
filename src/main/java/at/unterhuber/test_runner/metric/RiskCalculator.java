package at.unterhuber.test_runner.metric;

import at.unterhuber.test_runner.git.Apriori;
import at.unterhuber.test_runner.git.GitStats;
import at.unterhuber.test_runner.util.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class RiskCalculator {
    private final MetricMeasure measure;
    private final Config config;

    private HashMap<String, Double> risks;
    private List<Apriori.Combination<String>> combinations = new ArrayList<>();

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
        System.out.println("Risks\n" + toLineSeparatedString(risks) + "\n");
        shareRiskOftenChangedTogether();
        addStatisticalRisks();
        return risks;
    }

    private void addStatisticalRisks() {
        if (stats == null) {
            return;
        }
        int maxChanges = stats.getMaxChanges();
        int maxContributors = stats.getMaxContributors();
        long oldestDate = stats.getOldestDate().getTime();
        long currentDate = new Date().getTime();
        double timeSpan = (double) currentDate - oldestDate;
        for (String clazz : risks.keySet()) {
            double risk = risks.get(clazz);
            if (stats.creationOf(clazz).equals(new Date(0L))) {
                continue;
            }
            long lastModificationDate = stats.lastModificationOf(clazz).getTime();
            long creationDate = stats.creationOf(clazz).getTime();
            double span = currentDate - creationDate;
            double riskRecentlyChanged = (lastModificationDate - currentDate + span) / span;
            double riskNew = (creationDate - oldestDate) / timeSpan;
            double riskOftenChanged = stats.changeCountOf(clazz) / (double) maxChanges;
            double riskManyChanged = stats.contributorCountOf(clazz) / (double) maxContributors;
            risk += riskRecentlyChanged * config.getWeightOf("recency")
                    + riskNew * config.getWeightOf("new")
                    + riskOftenChanged * config.getWeightOf("changes")
                    + riskManyChanged * config.getWeightOf("contributors");
            risks.put(clazz, risk);
        }
        System.out.println("Risks (with files sharing risk + statistical risks)\n" + toLineSeparatedString(risks) + "\n");
    }

    public void shareRiskOftenChangedTogether() {
        double propagationFactor = config.getPropagationFactor();
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
                if (risk != null && averageRisk > risk) {
                    risk = (risk + averageRisk * propagationFactor) / (1 + propagationFactor);
                    risks.put(right, risk);
                }
            }
        }
        System.out.println("Risks (with files sharing risk)\n" + toLineSeparatedString(risks) + "\n");
    }

    public void setCombinations(List<Apriori.Combination<String>> combinations) {
        this.combinations = combinations;
    }

    public void setStats(GitStats stats) {
        this.stats = stats;
    }
}
