package at.unterhuber.test_runner.util;

import at.unterhuber.test_runner.path.ProjectPathHandler;

public class DummyClassLoader extends FileClassLoader {
    public DummyClassLoader() {
        super(null);
    }

    @Override
    public boolean isTestClass(String clazz) {
        return clazz.contains("Test");
    }
}
