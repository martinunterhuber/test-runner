package at.unterhuber.test_runner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileClassLoader {
    private final String path;
    private final String packageName;
    private final ClassLoader cl;
    private List<? extends Class<?>> classes;

    public FileClassLoader(String path, String packageName, ClassLoader cl) {
        this.path = path;
        this.packageName = packageName;
        this.cl = cl;
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
