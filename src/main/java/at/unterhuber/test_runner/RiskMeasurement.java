package at.unterhuber.test_runner;

public class RiskMeasurement {
    private final String className;
    private final RiskMetric metric;
    private final int value;

    // value relative to the maximum value of this metric (=value/max)
    private double relativeValue = 1.0;

    public RiskMeasurement(String className, RiskMetric metric, int value) {
        this.className = className;
        this.metric = metric;
        this.value = value;
    }

    public void forMaxValue(double max) {
        if (value != 0) {
            relativeValue = (double) value / max;
        }
    }

    public double getRelativeValue() {
        return relativeValue;
    }

    public String getClassName() {
        return className;
    }
}
