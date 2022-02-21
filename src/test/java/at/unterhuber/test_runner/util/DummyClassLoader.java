package at.unterhuber.test_runner.util;

public class DummyClassLoader extends FileClassLoader {
    public DummyClassLoader() {
        super(null);
    }

    @Override
    public boolean isTestClass(String clazz) {
        return clazz.contains("Test");
    }
}
