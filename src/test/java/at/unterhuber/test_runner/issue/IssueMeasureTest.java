package at.unterhuber.test_runner.issue;

import at.unterhuber.test_runner.path.GradlePathHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class IssueMeasureTest {
    private IssueMeasure issueMeasure;

    @BeforeEach
    public void setup() throws IOException {
        issueMeasure = new IssueMeasure(new GradlePathHandler(System.getProperty("user.dir") + "/testProject"));
        issueMeasure.findIssues();
    }

    @Test
    public void test_getIssues() {
        Assertions.assertEquals(1, issueMeasure.getIssues().size());
        Assertions.assertTrue(issueMeasure.getIssues().containsKey("at.unterhuber.test.Main"));
    }

    @Test
    public void test_getTestIssues() {
        Assertions.assertEquals(3, issueMeasure.getTestIssues().size());
        Assertions.assertTrue(issueMeasure.getTestIssues().containsKey("at.unterhuber.test.subpackage.Test3"));
        Assertions.assertTrue(issueMeasure.getTestIssues().containsKey("at.unterhuber.test.Test2"));
        Assertions.assertTrue(issueMeasure.getTestIssues().containsKey("at.unterhuber.test.Junit4Test1"));
    }

    @AfterEach
    public void cleanup() {
        issueMeasure = null;
    }
}
