package at.unterhuber.test_runner;

public class SonarIssue {
    public final String component;
    public final Type type;
    public final Severity severity;

    public SonarIssue(String component, Type type, Severity severity) {
        this.component = component;
        this.type = type;
        this.severity = severity;
    }

    public int computeRisk() {
        return severity.risk * type.risk;
    }

    @Override
    public String toString() {
        return "SonarIssue{" +
                "component='" + component + '\'' +
                ", type=" + type +
                ", severity=" + severity +
                '}';
    }

    public enum Type {
        CODE_SMELL(1), BUG(5), VULNERABILITY(5);

        public final int risk;

        Type(int risk) {
            this.risk = risk;
        }
    }

    public enum Severity {
        INFO(1), MINOR(2), MAJOR(5), CRITICAL(10), BLOCKER(20);

        public final int risk;

        Severity(int risk) {
            this.risk = risk;
        }
    }
}
