package at.unterhuber.test_runner.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class ConfigTest {
    private Config config;

    @BeforeEach
    public void setup() {
        config = new Config(Path.of("src/test/resources"), Path.of(""));
        config.loadConfigFromFile();
    }

    @Test
    public void testGetWeightOf() {
        Assertions.assertEquals(0.5, config.getWeightOf("Loc"));
        Assertions.assertEquals(2, config.getWeightOf("Wmc"));
    }

    @Test
    public void testInvalidPath_shouldFallbackToDefault() {
        config = new Config(Path.of("src"), Path.of(""));
        config.loadConfigFromFile();
    }

    @Test
    public void testInvalidPaths_shouldThrow() {
        config = new Config(Path.of("src"), Path.of("src"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> config.loadConfigFromFile());
    }

    @Test
    public void testThresholds() {
        Assertions.assertEquals(3.0, config.getMetricThreshold());
        Assertions.assertEquals(1.5, config.getTestMetricThreshold());
        Assertions.assertEquals(5, config.getIssueThreshold());
        Assertions.assertEquals(5, config.getTestIssueThreshold());
        Assertions.assertEquals(5, config.getBugThreshold());
        Assertions.assertEquals(5, config.getTestBugThreshold());
    }

    @AfterEach
    public void cleanup() {
        config = null;
    }
}
