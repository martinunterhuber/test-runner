package at.unterhuber.test_runner;

public class GitFileChange {
    private final String clazz;
    private final int added;
    private final int removed;

    public GitFileChange(String clazz, int added, int removed) {
        this.clazz = clazz;
        this.added = added;
        this.removed = removed;
    }

    @Override
    public String toString() {
        return String.format("%s: +%d -%d", clazz, added, removed);
    }
}
