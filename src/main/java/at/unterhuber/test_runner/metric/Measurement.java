package at.unterhuber.test_runner.metric;

public class Measurement {
    private final String className;
    private final Metric metric;
    private final int value;

    // value relative to the maximum value of this metric (=value/max)
    private double relativeValue = 1.0;

    public Measurement(String className, Metric metric, int value) {
        this.className = className;
        this.metric = metric;
        this.value = value;
    }

    public void forMaxValue(double max) {
        if (max != 0) {
            relativeValue = (double) value / max;
        }
    }

    public double getRelativeValue() {
        return relativeValue;
    }

    public String getClassName() {
        return className;
    }

    public String getMetric() {
        return metric.name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "className='" + className + '\'' +
                ", metric=" + metric.name +
                ", value=" + value +
                ", relativeValue=" + relativeValue +
                '}';
    }
}
