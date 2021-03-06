package at.unterhuber.test_runner.path;

import java.nio.file.Path;

public class GradlePathHandler implements ProjectPathHandler {
    private final Path rootPath;

    public GradlePathHandler(String rootPath) {
        this.rootPath = Path.of(rootPath);
    }

    @Override
    public Path getRootPath() {
        return rootPath;
    }

    @Override
    public Path getSourcePath() {
        return rootPath.resolve("src");
    }

    @Override
    public Path getClassPath() {
        return rootPath.resolve("build/classes/java");
    }

    @Override
    public Path getMainSourcePath() {
        return getSourcePath().resolve("main/java");
    }

    @Override
    public Path getMainClassPath() {
        return getClassPath().resolve("main");
    }

    @Override
    public Path getTestSourcePath() {
        return getSourcePath().resolve("test/java");
    }

    @Override
    public Path getTestClassPath() {
        return getClassPath().resolve("test");
    }

    @Override
    public boolean isMainSourcePath(String absolutePath) {
        if (!Path.of(absolutePath).isAbsolute()) {
            return false;
        }
        return !getMainSourcePath().relativize(Path.of(absolutePath)).startsWith("../");
    }

    @Override
    public boolean isTestSourcePath(String absolutePath) {
        if (!Path.of(absolutePath).isAbsolute()) {
            return false;
        }
        return !getTestSourcePath().relativize(Path.of(absolutePath)).startsWith("../");
    }
}
