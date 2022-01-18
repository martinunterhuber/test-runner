package at.unterhuber.test_runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
    private final Map<String, Double> weights = new HashMap<>();
    private final Path path;
    private final Path selfPath;

    private double metricThreshold;
    private double testMetricThreshold;
    private int issueThreshold;
    private int testIssueThreshold;

    public Config(Path path, Path selfPath) {
        this.path = path;
        this.selfPath = selfPath;
    }

    public void loadConfig() {
        File file = path.resolve("test.properties").toFile();
        if (!file.exists()) {
            file = selfPath.resolve("test.properties").toFile();
        }
        try (InputStream input = new FileInputStream(file)) {
            Properties prop = new Properties();
            prop.load(input);
            for (String property : prop.stringPropertyNames()) {
                if (property.startsWith("weight")) {
                    weights.put(property.split("_")[1], Double.parseDouble(prop.getProperty(property)));
                }
            }
            metricThreshold = Double.parseDouble(prop.getProperty("metricThreshold"));
            testMetricThreshold = Double.parseDouble(prop.getProperty("testMetricThreshold"));
            issueThreshold = Integer.parseInt(prop.getProperty("issueThreshold"));
            testIssueThreshold = Integer.parseInt(prop.getProperty("testIssueThreshold"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Double getWeightOf(String key) {
        return weights.get(key);
    }

    public double getMetricThreshold() {
        return metricThreshold;
    }

    public void setMetricThreshold(double metricThreshold) {
        this.metricThreshold = metricThreshold;
    }

    public double getTestMetricThreshold() {
        return testMetricThreshold;
    }

    public void setTestMetricThreshold(double testMetricThreshold) {
        this.testMetricThreshold = testMetricThreshold;
    }

    public int getIssueThreshold() {
        return issueThreshold;
    }

    public void setIssueThreshold(int issueThreshold) {
        this.issueThreshold = issueThreshold;
    }

    public int getTestIssueThreshold() {
        return testIssueThreshold;
    }

    public void setTestIssueThreshold(int testIssueThreshold) {
        this.testIssueThreshold = testIssueThreshold;
    }
}
