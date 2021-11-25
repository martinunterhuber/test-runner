package at.unterhuber.test_runner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileClassLoader {
    private final Path rootPath;
    private final Path testSourcePath;
    private final Path testClassPath;
    private final Path mainClassPath;
    private ClassLoader classLoader;
    private List<? extends Class<?>> classes;

    public FileClassLoader(String rootPath) {
        // For now these paths only work for Gradle projects
        this.rootPath = Path.of(rootPath);
        this.testSourcePath = this.rootPath.resolve("src/test/java");
        Path classPath = this.rootPath.resolve("build/classes/java");
        this.mainClassPath = classPath.resolve("main");
        this.testClassPath = classPath.resolve("test");
    }

    public String getFullClassNameFrom(Path path) {
        Path relative = testSourcePath.relativize(path);
        return relative.toString().replace("/", ".").replace(".java", "");
    }

    public void initClassLoader() throws MalformedURLException {
        URL[] urls = new URL[]{
                testClassPath.toFile().toURI().toURL(),
                mainClassPath.toFile().toURI().toURL()
        };
        classLoader = new URLClassLoader(urls);
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public void loadClasses() throws IOException {
        classes = Files
                .walk(testSourcePath)
                .filter(Files::isRegularFile)
                .map(this::getFullClassNameFrom)
                .map(className -> {
                    try {
                        System.out.println(className);
                        return classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).collect(Collectors.toList());
    }

    public List<? extends Class<?>> getClasses() {
        return classes;
    }

    public List<Field> getFields() {
        return classes
                .stream()
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredFields()))
                .collect(Collectors.toList());
    }
}
