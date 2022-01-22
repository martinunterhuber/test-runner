package at.unterhuber.test_runner;

import java.util.List;
import java.util.Map;

public class DummyMetricMeasure extends MetricMeasure{
    public DummyMetricMeasure() {
        super(null, null);
    }

    @Override
    public Map<String, List<Measurement>> getMeasurements() {
        Metric metric1 = new Metric("Wmc");
        Measurement m1 = new Measurement("C1", metric1, 1);
        m1.forMaxValue(2);
        Measurement m2 = new Measurement("C2", metric1, 2);
        m2.forMaxValue(2);
        Metric metric2 = new Metric("Rfc");
        Measurement m3 = new Measurement("C1", metric2, 8);
        m1.forMaxValue(8);
        Measurement m4 = new Measurement("C2", metric2, 2);
        m2.forMaxValue(8);
        return Map.of("C1", List.of(m1, m3), "C2", List.of(m2, m4));
    }
}
