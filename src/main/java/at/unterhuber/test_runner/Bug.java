package at.unterhuber.test_runner;

import edu.umd.cs.findbugs.BugInstance;

public class Bug {
    private final BugInstance bug;

    public Bug(BugInstance bug) {
        this.bug = bug;
    }

    public int computeRisk() {
        int rank = bug.getBugRank();

        // 1 (high priority) <= rank <= 20 (low priority)
        return 20 - rank;
    }
}
