package at.unterhuber.test_runner.bug;

import at.unterhuber.test_runner.bug.BugsMeasure;
import at.unterhuber.test_runner.path.GradlePathHandler;
import at.unterhuber.test_runner.path.ProjectPathHandler;
import at.unterhuber.test_runner.util.FileClassLoader;
import edu.umd.cs.findbugs.Priorities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BugMeasureTest {
    private BugsMeasure bugMeasure;

    @BeforeEach
    public void setup() throws InterruptedException, IOException, ClassNotFoundException {
        ProjectPathHandler handler = new GradlePathHandler(System.getProperty("user.dir") + "/testProject");
        FileClassLoader cl = new FileClassLoader(handler);
        cl.initClassLoader();
        cl.loadTestClasses();
        bugMeasure = new BugsMeasure(handler, cl, Priorities.NORMAL_PRIORITY);
        bugMeasure.findBugs();
    }

    @Test
    public void test_getBugs() {
        Assertions.assertEquals(1, bugMeasure.getBugs().size());
    }

    @Test
    public void test_getTestBugs() {
        Assertions.assertEquals(1, bugMeasure.getTestBugs().size());
    }

    @AfterEach
    public void cleanup() {
        bugMeasure = null;
    }
}