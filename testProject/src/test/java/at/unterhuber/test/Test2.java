package at.unterhuber.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test2 {
    @Test
    public void test_3() {
        System.out.println(new String("Hello World"));
        Assertions.assertEquals(1, 2);
    }

    @Test
    public void test_4() {
        Assertions.assertEquals(1, 1);
    }
}
