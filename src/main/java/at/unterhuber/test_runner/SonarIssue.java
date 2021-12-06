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

    public enum Type {CODE_SMELL, BUG, VULNERABILITY}
    public enum Severity {INFO, MINOR, MAJOR, CRITICAL, BLOCKER}

    @Override
    public String toString() {
        return "SonarIssue{" +
                "component='" + component + '\'' +
                ", type=" + type +
                ", severity=" + severity +
                '}';
    }
}
