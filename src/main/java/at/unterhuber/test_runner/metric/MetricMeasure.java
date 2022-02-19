package at.unterhuber.test_runner.metric;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKNotifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricMeasure {
    private final Metric[] metrics;
    private final Map<String, CKClassResult> ckMeasurements;
    private final Map<String, List<Measurement>> measurements;
    private final String path;

    public MetricMeasure(String path, Metric[] metrics) {
        this.path = path;
        this.metrics = metrics;
        this.ckMeasurements = new HashMap<>();
        this.measurements = new HashMap<>();
    }

    public void measure() {
        new CK().calculate(path, new CKNotifier() {
            @Override
            public void notify(CKClassResult result) {
                ckMeasurements.put(result.getClassName(), result);
            }

            @Override
            public void notifyError(String sourceFilePath, Exception e) {
                System.err.println("Error in " + sourceFilePath);
                e.printStackTrace(System.err);
            }
        });
    }

    public void initMeasurements() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Metric metric : metrics) {
            List<Measurement> metricMeasurements = new ArrayList<>();
            measurements.put(metric.name, metricMeasurements);
            int max = 0;
            for (CKClassResult result : ckMeasurements.values()) {
                Method method = CKClassResult.class.getMethod("get" + metric.name);
                int value = (int) method.invoke(result);
                metricMeasurements.add(new Measurement(result.getClassName(), metric, value));
                if (value > max) {
                    max = value;
                }
            }

            for (Measurement measurement : metricMeasurements) {
                measurement.forMaxValue(max);
            }
        }
        // printSelectedMetrics();
    }

    public void printSelectedMetrics() {
        for (Map.Entry<String, List<Measurement>> entry : measurements.entrySet()) {
            for (Measurement measurement : entry.getValue()) {
                System.out.printf("%55s - %s: %s\n", measurement.getClassName(), measurement.getMetric(), measurement.getRelativeValue());
            }
        }
    }

    public Map<String, List<Measurement>> getMeasurements() {
        return measurements;
    }
}
