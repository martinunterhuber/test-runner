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

    default String getFullClassNameFrom(Path path) {
        Path relativeMain = getMainSourcePath().relativize(getRootPath().resolve(path));
        Path relativeTest = getTestSourcePath().relativize(getRootPath().resolve(path));
        Path relative = relativeMain.toString().startsWith("../") ? relativeTest : relativeMain;
        return relative.toString().replace("/", ".").replace(".java", "");
    }

    default String pathToFullClassName(String filePath) {
        Path path = getRootPath().resolve(filePath);
        Path mainPath;
        Path testPath;
        String extension;
        if (filePath.endsWith(".java")) {
            mainPath = getMainSourcePath();
            testPath = getTestSourcePath();
            extension = ".java";
        } else {
            mainPath = getMainClassPath();
            testPath = getTestClassPath();
            extension = ".class";
        }
        String relativePath = mainPath.relativize(path).toString();
        if (relativePath.startsWith("../")) {
            relativePath = testPath.relativize(path).toString();
            if (relativePath.startsWith("../")) {
                return null;
            }
        }
        return relativePath.replace(extension, "").replace("/", ".");
    }
}
