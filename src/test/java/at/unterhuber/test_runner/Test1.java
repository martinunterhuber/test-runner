package at.unterhuber.test_runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test1 {
    @Test
    public void test_1() {
        Assertions.assertEquals(1, 2);
    }

    @Test
    public void test_2() {
        Assertions.assertEquals(1, 1);
    }
}
