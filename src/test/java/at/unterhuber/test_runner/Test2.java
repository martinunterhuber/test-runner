package at.unterhuber.test_runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test2 {
    @Test
    public void test_3() {
        Assertions.assertEquals(1, 2);
    }

    @Test
    public void test_4() {
        Assertions.assertEquals(1, 1);
    }
}
