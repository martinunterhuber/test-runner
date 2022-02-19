package at.unterhuber.test_runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class GitStatsTest {
    private GitStats stats;

    @BeforeEach
    public void setup() {
        GitCommit commit1 = new GitCommit("123456", "Martin", 4);
        GitCommit commit2 = new GitCommit("234567", "Test", 3);
        GitCommit commit3 = new GitCommit("345678", "Other", 2);
        GitCommit commit4 = new GitCommit("456789", "Martin", 1);
        commit1.addFileChange("at.unterhuber.test.Test1", 1, 20, 5);
        commit2.addFileChange("at.unterhuber.test.Test2", 2, 10, 0);
        commit3.addFileChange("at.unterhuber.test.Test1", 1, 0, 10);
        commit3.addFileChange("at.unterhuber.test.Test2", 2, 20, 0);
        commit4.addFileChange("at.unterhuber.test.Test1", 1, 20, 0);
        List<GitCommit> commits = List.of(commit1, commit2, commit3, commit4);
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "at.unterhuber.test.Test1");
        map.put(2, "at.unterhuber.test.Test2");
        stats = new GitStats(commits, map);
        stats.initStats();
    }

    @Test
    public void testGetOldestDate() {
        Assertions.assertEquals(new Date(1000L), stats.getOldestDate());
    }

    @Test
    public void testGetMaxChanges() {
        Assertions.assertEquals(3, stats.getMaxChanges());
    }

    @Test
    public void testGetMaxContributors() {
        Assertions.assertEquals(2, stats.getMaxContributors());
    }

    @Test
    public void testCreationOf() {
        Assertions.assertEquals(new Date(1000L), stats.creationOf("at.unterhuber.test.Test1"));
        Assertions.assertEquals(new Date(2000L), stats.creationOf("at.unterhuber.test.Test2"));
    }

    @Test
    public void testCreationOf_doesNotExist() {
        Assertions.assertEquals(new Date(0L), stats.creationOf("at.unterhuber.test.DoesNotExist"));
    }

    @Test
    public void testChangeCountOf() {
        Assertions.assertEquals(3, stats.changeCountOf("at.unterhuber.test.Test1"));
        Assertions.assertEquals(2, stats.changeCountOf("at.unterhuber.test.Test2"));
    }

    @Test
    public void testChangeCountOf_doesNotExist() {
        Assertions.assertEquals(0, stats.changeCountOf("at.unterhuber.test.DoesNotExist"));
    }

    @Test
    public void testContributorCount() {
        Assertions.assertEquals(2, stats.contributorCountOf("at.unterhuber.test.Test1"));
        Assertions.assertEquals(2, stats.contributorCountOf("at.unterhuber.test.Test2"));
    }

    @Test
    public void testContributorCount_doesNotExist() {
        Assertions.assertEquals(0, stats.contributorCountOf("at.unterhuber.test.DoesNotExist"));
    }

    @Test
    public void testContributorsOf() {
        Assertions.assertEquals(Set.of("Martin", "Other"), stats.contributorsOf("at.unterhuber.test.Test1"));
        Assertions.assertEquals(Set.of("Test", "Other"), stats.contributorsOf("at.unterhuber.test.Test2"));
    }

    @Test
    public void testContributorsOf_doesNotExist() {
        Assertions.assertEquals(Set.of(), stats.contributorsOf("at.unterhuber.test.DoesNotExist"));
    }

    @Test
    public void testLastModificationOf() {
        Assertions.assertEquals(new Date(4000L), stats.lastModificationOf("at.unterhuber.test.Test1"));
        Assertions.assertEquals(new Date(3000L), stats.lastModificationOf("at.unterhuber.test.Test2"));
    }

    @Test
    public void testLastModificationOf_doesNotExist() {
        Assertions.assertEquals(new Date(0L), stats.lastModificationOf("at.unterhuber.test.DoesNotExist"));
    }
}
