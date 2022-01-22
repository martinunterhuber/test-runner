package at.unterhuber.test.subpackage;

import at.unterhuber.test.ClassToTest;

public class AnotherClassToTest extends ClassToTest {
    public int min(int a, int b, int c) {
        if (a < b) {
            if (c < a) {
                return c;
            }
            return a;
        }
        if (c < b){
            return c;
        }
        return b;
    }

    public int min(int a, int b) {
        if (a > b) {
            return b;
        }
        return a;
    }

    public int max(int a, int b) {
        if (a > b) {
            return a;
        }
        return b;
    }
}
