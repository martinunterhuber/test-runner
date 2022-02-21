package at.unterhuber.test_runner.bug;

import edu.umd.cs.findbugs.BugInstance;

public class Bug {
    private final BugInstance bug;
    private int bugRank;

    public Bug(BugInstance bug) {
        this.bug = bug;
    }

    public Bug(int bugRank) {
        this.bug = null;
        this.bugRank = bugRank;
    }

    public int computeRisk() {
        if (bug != null) {
            bugRank = bug.getBugRank();
        }

        // 1 (high priority) <= rank <= 20 (low priority)
        return 20 - bugRank;
    }
}
