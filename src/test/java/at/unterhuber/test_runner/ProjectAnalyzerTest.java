package at.unterhuber.test_runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ProjectAnalyzerTest {
    @Test
    public void testAnalyzeProject() throws IOException, InterruptedException {
        List<String> expected = List.of("at.unterhuber.test.subpackage.Test3", "at.unterhuber.test.Test2", "at.unterhuber.test.Junit4Test1");

        List<String> testToRun = ProjectAnalyzer.analyzeProject(
                System.getProperty("user.dir") + "/testProject",
                "at.unterhuber.test",
                System.getProperty("user.dir"),
                "",
                System.getProperty("user.dir") + "/testProject"
        );

        Assertions.assertEquals(expected, testToRun);
    }
}
