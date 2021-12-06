package at.unterhuber.test_runner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileClassLoader {
    private final ProjectPathHandler pathHandler;
    private ClassLoader classLoader;
    private List<? extends Class<?>> testClasses;

    public FileClassLoader(ProjectPathHandler pathHandler) {
        this.pathHandler = pathHandler;
    }

    public String getFullTestClassNameFrom(Path path) {
        Path relative = pathHandler.getTestSourcePath().relativize(path);
        return relative.toString().replace("/", ".").replace(".java", "");
    }

    public String getFullMainClassNameFrom(Path path) {
        Path relative = pathHandler.getMainSourcePath().relativize(pathHandler.getRootPath().resolve(path));
        return relative.toString().replace("/", ".").replace(".java", "");
    }

    public String getFullClassNameFrom(Path path) {
        Path relativeMain = pathHandler.getMainSourcePath().relativize(pathHandler.getRootPath().resolve(path));
        Path relativeTest = pathHandler.getTestSourcePath().relativize(pathHandler.getRootPath().resolve(path));
        Path relative = relativeMain.toString().startsWith("../") ? relativeTest : relativeMain;
        return relative.toString().replace("/", ".").replace(".java", "");
    }

    public void initClassLoader() throws MalformedURLException {
        URL[] urls = new URL[]{
                pathHandler.getTestClassPath().toFile().toURI().toURL(),
                pathHandler.getMainClassPath().toFile().toURI().toURL()
        };
        classLoader = new URLClassLoader(urls);
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public void loadTestClasses() throws IOException {
        testClasses = Files
                .walk(pathHandler.getTestSourcePath())
                .filter(Files::isRegularFile)
                .map(this::getFullTestClassNameFrom)
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

    public Map<String, List<VariableDeclarator>> getTestClassVariables() throws IOException {
        return Files
                .walk(pathHandler.getTestSourcePath())
                .filter(Files::isRegularFile)
                .collect(Collectors.toMap(this::getFullTestClassNameFrom, className -> {
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(className.toFile());
                        return cu.findAll(VariableDeclarator.class);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return new ArrayList<>();
                    }
                }));
    }

    public List<? extends Class<?>> getTestClasses() {
        return testClasses;
    }

    public List<Field> getTestFields() {
        return testClasses
                .stream()
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredFields()))
                .collect(Collectors.toList());
    }
}
