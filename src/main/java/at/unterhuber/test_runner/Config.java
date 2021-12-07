package at.unterhuber.test_runner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
    private final Map<String, Double> weights = new HashMap<>();
    private final Path config;
    private final String[] metrics;

    public Config(Path config, String[] metrics) {
        this.config = config;
        this.metrics = metrics;
    }

    public void loadConfig() {
        try (InputStream input = new FileInputStream(config.resolve("test.properties").toFile())) {
            Properties prop = new Properties();
            prop.load(input);
            for (String key : metrics) {
                weights.put(key, Double.parseDouble(prop.getProperty(key)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Double getWeightOf(String key) {
        return weights.get(key);
    }
}
