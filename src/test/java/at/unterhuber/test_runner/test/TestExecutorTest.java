package at.unterhuber.test_runner.test;

import at.unterhuber.test_runner.test.DummyTestSelector;
import at.unterhuber.test_runner.test.TestExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestExecutorTest {
    private TestExecutor executor;

    @BeforeEach
    public void setup() {
        executor = new TestExecutor(new DummyTestSelector());
    }

    @Test
    public void test_executesFailingTest_shouldThrow() {
        Assertions.assertThrows(RuntimeException.class, () -> executor.executeTests());
    }

    @AfterEach
    public void cleanup() {
        executor = null;
    }
}
