package at.unterhuber.test_runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestExecutorTest {
    @Test
    public void testExecutesFailingTest_shouldThrow() {
        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> TestExecutor.executeTests(new String[]{"at.unterhuber.test.Test2"})
        );
        Assertions.assertEquals("Some tests failed!", exception.getMessage());
    }

    @Test
    public void testExecutesValidTest_shouldNotThrow() {
        Assertions.assertDoesNotThrow(
                () -> TestExecutor.executeTests(new String[]{"at.unterhuber.test.subpackage.Test3"})
        );
    }

    @Test
    public void testExecutesFailingAndValidTest_shouldThrow() {
        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> TestExecutor.executeTests(new String[]{"at.unterhuber.test.subpackage.Test3", "at.unterhuber.test.Test2"})
        );
        Assertions.assertEquals("Some tests failed!", exception.getMessage());
    }
}
