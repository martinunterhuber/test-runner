package at.unterhuber.test_runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileClassLoader {
    private final String path;
    private final String packageName;
    private ClassLoader cl;
    private List<? extends Class<?>> classes;

    public FileClassLoader(String path, String packageName) {
        this.path = path;
        this.packageName = packageName;
    }

    public void initClassLoader() throws MalformedURLException {
        File testDir = new File(path + "build/classes/java/test");
        File mainDir = new File(path + "build/classes/java/main");
        URL[] urls = new URL[]{testDir.toURI().toURL(), mainDir.toURI().toURL()};
        cl = new URLClassLoader(urls);
        Thread.currentThread().setContextClassLoader(cl);
    }

    public void loadClasses() throws IOException {
        classes = Files
                .walk(Paths.get(path + "build/classes/java/test"))
                .filter(Files::isRegularFile)
                .map(fileName -> packageName + "." + fileName.getFileName().toString().replace(".class", ""))
                .map(className -> {
                    try {
                        return cl.loadClass(className);
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
