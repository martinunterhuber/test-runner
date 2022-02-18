package at.unterhuber.test_runner;

public class GitFileChange {
    private final String clazz;
    private final int id;
    private final int added;
    private final int removed;

    public GitFileChange(String clazz, int id, int added, int removed) {
        this.clazz = clazz;
        this.id = id;
        this.added = added;
        this.removed = removed;
    }

    @Override
    public String toString() {
        return String.format("%s: +%d -%d", getClazz(), getAdded(), getRemoved());
    }

    public String getClazz() {
        return clazz;
    }

    public int getId() {
        return id;
    }

    public int getAdded() {
        return added;
    }

    public int getRemoved() {
        return removed;
    }
}
