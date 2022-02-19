package at.unterhuber.test_runner.git;

import at.unterhuber.test_runner.git.GitCommit;
import at.unterhuber.test_runner.git.GitFileChange;
import at.unterhuber.test_runner.git.GitParser;
import at.unterhuber.test_runner.git.GitStats;
import at.unterhuber.test_runner.path.GradlePathHandler;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class GitParserTest {
    private static String log;
    private GitParser parser;

    @BeforeAll
    public static void readLog() throws IOException {
        log = Files.readString(Path.of(System.getProperty("user.dir")).resolve("src/test/resources/gitlog.txt"));
    }

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        parser = new GitParser(new GradlePathHandler(System.getProperty("user.dir") + "/testProject"), log);
        parser.parseLog();
    }

    @Test
    public void testCommitData() {
        List<GitCommit> commits = parser.getCommits();
        GitCommit commit = commits.get(0);
        Assertions.assertEquals(new Date(1645204047000L), commit.getDate());
        Assertions.assertEquals("0748f7f", commit.getHash());
        Assertions.assertEquals("m.unterhuber99@gmail.com", commit.getAuthor());
    }

    @Test
    public void testChangeData() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(1).getChanges();
        Assertions.assertEquals(1, changes.size());
        GitFileChange change = changes.get(0);
        Assertions.assertEquals("at.unterhuber.test.Test2", change.getClazz());
        Assertions.assertEquals(1, change.getAdded());
        Assertions.assertEquals(0, change.getRemoved());
        Assertions.assertEquals(1, change.getId());
    }

    @Test
    public void testMovedClass() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(2).getChanges();
        GitFileChange change = changes.get(0);
        Assertions.assertEquals("at.unterhuber.test.Junit4Test1", change.getClazz());
    }

    @Test
    public void testMovedClass_linkedToNewClassName() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(3).getChanges();
        GitFileChange change = changes.get(0);
        System.out.println(changes);
        Assertions.assertEquals("at.unterhuber.test.Junit4Test1", change.getClazz());
    }

    @Test
    public void testRenamedClass() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(2).getChanges();
        GitFileChange change = changes.get(1);
        Assertions.assertEquals("at.unterhuber.test.Testx", change.getClazz());
    }

    @Test
    public void testRenamedClass_linkedToNewClassName() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(3).getChanges();
        GitFileChange change = changes.get(1);
        Assertions.assertEquals("at.unterhuber.test.Testx", change.getClazz());
    }

    @Test
    public void testAbsoluteMovedClass() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(2).getChanges();
        GitFileChange change = changes.get(2);
        Assertions.assertEquals("at.unterhuber.test.Test2", change.getClazz());
    }

    @Test
    public void testAbsoluteMovedClass_linkedToNewClassName() {
        List<GitCommit> commits = parser.getCommits();
        List<GitFileChange> changes = commits.get(3).getChanges();
        GitFileChange change = changes.get(2);
        Assertions.assertEquals("at.unterhuber.test.Test2", change.getClazz());
    }

    @Test
    public void testGetStats() {
        GitStats stats = parser.getStats();
        Assertions.assertNotNull(stats);
    }
}
