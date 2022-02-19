package at.unterhuber.test_runner.git;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitCommit {
    private final String hash;
    private final String author;
    private final Date date;
    private final List<GitFileChange> changes;

    public GitCommit(String hash, String author, long timestamp) {
        this.hash = hash;
        this.author = author;
        this.date = new Date(timestamp * 1000L);
        this.changes = new ArrayList<>();
    }

    public void addFileChange(String clazz, int id, int added, int removed) {
        changes.add(new GitFileChange(clazz, id, added, removed));
    }

    public String getHash() {
        return hash;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("Commit %s: %s %s", hash, author, date);
    }

    public List<GitFileChange> getChanges() {
        return changes;
    }
}
