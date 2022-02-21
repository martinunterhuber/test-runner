package at.unterhuber.test_runner.issue;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

public class Issue {
    private final RuleViolation violation;
    private RulePriority priority;

    public Issue(RuleViolation violation) {
        this.violation = violation;
    }

    public Issue(RulePriority priority) {
        this.violation = null;
        this.priority = priority;
    }

    public int computeRisk() {
        if (violation != null) {
            priority = violation.getRule().getPriority();
        }
        int priorityValue = priority.getPriority();

        // Lowest priority has the highest value in PMD, this will reverse it
        return (RulePriority.LOW.getPriority() + 1) - priorityValue;
    }
}
