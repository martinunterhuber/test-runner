package at.unterhuber.test_runner;

public class GitFileChange {
    public final String clazz;
    public final int id;
    public final int added;
    public final int removed;

    public GitFileChange(String clazz, int id, int added, int removed) {
        this.clazz = clazz;
        this.id = id;
        this.added = added;
        this.removed = removed;
    }

    @Override
    public String toString() {
        return String.format("%s: +%d -%d", clazz, added, removed);
    }
}
