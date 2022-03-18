package at.unterhuber.test_runner.util;

import at.unterhuber.test_runner.path.ProjectPathHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FileClassLoader {
    private final ProjectPathHandler pathHandler;
    private ClassLoader classLoader;
    private List<? extends Class<?>> testClasses;
    private Set<String> testClassesNames;

    public FileClassLoader(ProjectPathHandler pathHandler) {
        this.pathHandler = pathHandler;
    }

    public String getFullTestClassNameFrom(Path path) {
        Path relative = pathHandler.getTestSourcePath().relativize(path);
        return relative.toString().replace("/", ".").replace(".java", "");
    }

    public void initClassLoader() throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        try {
            urls = Files.walk(pathHandler.getClassPath().resolve("dependency"))
                    .filter(path -> path.toString().endsWith(".jar"))
                    .map(path -> {
                        try {
                            return path.toUri().toURL();
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    }).collect(Collectors.toList());
        } catch (IOException ignored) {
        }
        urls.add(pathHandler.getTestClassPath().toFile().toURI().toURL());
        urls.add(pathHandler.getMainClassPath().toFile().toURI().toURL());
        urls.add(pathHandler.getClassPath().toFile().toURI().toURL());
        classLoader = new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public void loadTestClasses() throws IOException {
        testClassesNames = Files
                .walk(pathHandler.getTestSourcePath())
                .filter(Files::isRegularFile)
                .map(this::getFullTestClassNameFrom)
                .collect(Collectors.toSet());

        testClasses = testClassesNames
                .stream()
                .map(className -> {
                    try {
                        return classLoader.loadClass(className);
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        return null;
                    }
                }).collect(Collectors.toList());
    }

    public Set<String> getTestClassesNames() {
        return testClassesNames;
    }

    public boolean isTestClass(String clazz) {
        return testClassesNames.contains(clazz);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
