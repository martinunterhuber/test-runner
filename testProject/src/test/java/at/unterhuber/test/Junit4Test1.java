package at.unterhuber.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Junit4Test1 {
    private ClassToTest cls = new ClassToTest();

    @Test
    public void test_subtract10And4_shouldReturn6() {
        assertEquals(6, cls.subtract(10, 4));
    }

    @Test
    public void test_add10And4_shouldReturn14() {
        assertEquals(14, cls.add(10, 4));
    }
}
