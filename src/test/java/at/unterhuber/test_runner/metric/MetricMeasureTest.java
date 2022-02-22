package at.unterhuber.test_runner.metric;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricMeasureTest {
    private MetricMeasure metricMeasure;

    @BeforeEach
    public void setup() {
        metricMeasure = new MetricMeasure(System.getProperty("user.dir") + "/testProject/src/main", new Metric[]{new Metric("Wmc")});
    }

    @Test
    public void testGetMeasurement() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Double> expected = List.of(0.375, 0.125, 1d, 0d);
        metricMeasure.measure();
        metricMeasure.initMeasurements();
        Map<String, List<Measurement>> measurements = metricMeasure.getMeasurements();
        Assertions.assertEquals(expected, measurements.get("Wmc").stream().map(Measurement::getRelativeValue).collect(Collectors.toList()));
    }

    @AfterEach
    public void cleanup() {
        metricMeasure = null;
    }
}
