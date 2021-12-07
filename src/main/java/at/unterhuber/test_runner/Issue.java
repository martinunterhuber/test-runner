package at.unterhuber.test_runner;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

public class Issue {
    private final RuleViolation violation;

    public Issue(RuleViolation violation) {
        this.violation = violation;
    }

    public int computeRisk() {
        int priority = violation.getRule().getPriority().getPriority();

        // Lowest priority has the highest value in PMD, this will reverse it
        return (RulePriority.LOW.getPriority() + 1) - priority;
    }
}
