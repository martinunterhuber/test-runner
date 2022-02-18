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
    private int bugThreshold;
    private int testBugThreshold;
    private double propagationFactor;

    public Config(Path path, Path selfPath) {
        this.path = path;
        this.selfPath = selfPath;
    }

    public void loadConfigFromFile() {
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
            bugThreshold = Integer.parseInt(prop.getProperty("bugThreshold"));
            testBugThreshold = Integer.parseInt(prop.getProperty("testBugThreshold"));
            propagationFactor = Double.parseDouble(prop.getProperty("filePropagationFactor"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Neither this nor the other project has a valid test.properties", e);
        }
    }

    public Double getWeightOf(String key) {
        return weights.getOrDefault(key, 0.0);
    }

    public double getMetricThreshold() {
        return metricThreshold;
    }

    public double getTestMetricThreshold() {
        return testMetricThreshold;
    }

    public int getIssueThreshold() {
        return issueThreshold;
    }

    public int getTestIssueThreshold() {
        return testIssueThreshold;
    }

    public int getBugThreshold() {
        return bugThreshold;
    }

    public int getTestBugThreshold() {
        return testBugThreshold;
    }

    public double getPropagationFactor() {
        return propagationFactor;
    }
}
