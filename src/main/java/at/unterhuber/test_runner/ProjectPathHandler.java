package at.unterhuber.test_runner;

import java.nio.file.Path;

public interface ProjectPathHandler {
    Path getRootPath();

    Path getTestSourcePath();

    Path getTestClassPath();

    Path getMainClassPath();

    Path getSourcePath();

    Path getMainSourcePath();

    Path getClassPath();

    boolean isMainSourcePath(String absolutePath);

    boolean isTestSourcePath(String absolutePath);
}
