package at.unterhuber.test_runner;

public class RiskMeasurement {
    private RiskMetric metric;
    private int value;

    // value relative to the maximum value of this metric (=value/max)
    private double relativeValue = 1.0;

    public RiskMeasurement(RiskMetric metric, int value) {
        this.metric = metric;
        this.value = value;
    }

    public void forMaxValue(int max) {
        if (value != 0) {
            relativeValue = (double) value / value;
        }
    }

    public double getRelativeValue() {
        return relativeValue;
    }
}
