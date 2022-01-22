package at.unterhuber.test.subpackage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test3 {
    private AnotherClassToTest cls = new AnotherClassToTest();

    @Test
    public void test_minOf4And13And6_shouldReturn4() {
        Assertions.assertEquals(4, cls.min(4, 13, 6));
    }
}
