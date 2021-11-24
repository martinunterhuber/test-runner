package at.unterhuber.test_runner;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKNotifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RiskCalculator {
    private final RiskMetric[] metrics;
    private final Map<String, CKClassResult> measurements;
    private final String path;

    public RiskCalculator(String path, RiskMetric[] metrics) {
        this.path = path;
        this.metrics = metrics;
        this.measurements = new HashMap<>();
    }

    public void measure() {
        new CK().calculate(path + "src/main/", new CKNotifier() {
            @Override
            public void notify(CKClassResult result) {
                measurements.put(result.getClassName(), result);
            }

            @Override
            public void notifyError(String sourceFilePath, Exception e) {
                System.err.println("Error in " + sourceFilePath);
                e.printStackTrace(System.err);
            }
        });
    }

    public void printSelectedMetrics() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for(Map.Entry<String, CKClassResult> entry : measurements.entrySet()) {
            for (RiskMetric metric : metrics) {
                Method method = CKClassResult.class.getMethod("get" + metric.name);
                System.out.println(entry.getKey() + " - " + metric.name + ": " + method.invoke(entry.getValue()));
            }
        }
    }
}
