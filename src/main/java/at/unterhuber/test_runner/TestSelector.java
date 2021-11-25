package at.unterhuber.test_runner;

import org.junit.platform.engine.DiscoverySelector;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void determineClassesToTest(HashMap<String, Double> risk) {
        classesToTest = risk.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<DiscoverySelector> selectTestClasses() {
        System.out.println(Arrays.toString(classesToTest.toArray()));
        List<Field> fields = loader.getFields();
        List<DiscoverySelector> selectors = fields.stream()
                .filter(field -> classesToTest.contains(field.getType().getCanonicalName()))
                .map(field -> selectClass(field.getDeclaringClass()))
                .collect(Collectors.toList());
        System.out.println(Arrays.toString(selectors.toArray()));
        return selectors;
    }
}
