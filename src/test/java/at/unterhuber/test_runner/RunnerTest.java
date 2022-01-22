package at.unterhuber.test_runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RunnerTest {
    @Test
    public void test_executesFailingTest_shouldThrow() {
        Assertions.assertThrows(RuntimeException.class, () -> Runner.main(new String[]{
                System.getProperty("user.dir") + "/testProject",
                "at.unterhuber.test",
                System.getProperty("user.dir"),
        }));
    }
}
