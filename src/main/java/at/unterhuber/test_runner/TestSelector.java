package at.unterhuber.test_runner;

import com.github.javaparser.ast.body.VariableDeclarator;
import org.junit.platform.engine.DiscoverySelector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestSelector {
    private final FileClassLoader loader;
    private List<String> classesToTest;
    private final double threshold;

    public TestSelector(FileClassLoader loader, double threshold) {
        this.loader = loader;
        this.threshold = threshold;
    }

    public void determineClassesToTest(HashMap<String, Double> risk, String[] changedFiles) {
        Set<String> changeSet = Arrays
                .stream(changedFiles)
                .map(file -> loader.getFullClassNameFrom(Path.of(file)))
                .collect(Collectors.toSet());

        classesToTest = risk.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > threshold)
                // .filter(entry -> changeSet.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<DiscoverySelector> selectTestClasses() {
        System.out.println(Arrays.toString(classesToTest.toArray()));
        List<Field> fields = loader.getTestFields();
        List<DiscoverySelector> selectors = fields.stream()
                .filter(field -> classesToTest.contains(field.getType().getCanonicalName()))
                .map(field -> selectClass(field.getDeclaringClass()))
                .collect(Collectors.toList());
        System.out.println(Arrays.toString(selectors.toArray()));
        return selectors;
    }
}
