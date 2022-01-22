package at.unterhuber.test_runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

public class RiskCalculatorTest {
    private RiskCalculator riskCalculator;
    private Config config;

    @BeforeEach
    public void setup() {
        config = new Config(Path.of("src/test/resources"), Path.of(""));
        config.loadConfigFromFile();
        riskCalculator = new RiskCalculator(new DummyMetricMeasure(), config);
    }

    @Test
    public void test_getMeasurement() {
        Map<String, Double> expected = Map.of("C1", 1.25, "C2", 1.5);
        Assertions.assertEquals(expected, riskCalculator.getRiskByClass());
    }

    @AfterEach
    public void cleanup() {
        config = null;
    }
}
