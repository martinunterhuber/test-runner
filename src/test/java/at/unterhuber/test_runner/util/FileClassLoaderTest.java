package at.unterhuber.test_runner.util;

import at.unterhuber.test_runner.path.GradlePathHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FileClassLoaderTest {
    private FileClassLoader classLoader;
    private static String ROOT = System.getProperty("user.dir") + "/testProject";

    @BeforeEach
    public void setup() throws MalformedURLException {
        classLoader = new FileClassLoader(new GradlePathHandler(ROOT));
        classLoader.initClassLoader();
    }

    @Test
    public void testFindsAllTestClasses() throws IOException {
        Set<String> expected = Set.of("at.unterhuber.test.subpackage.Test3", "at.unterhuber.test.Test2", "at.unterhuber.test.Junit4Test1");

        classLoader.loadTestClasses();

        Assertions.assertEquals(expected, classLoader.getTestClassesNames());
    }

    @Test
    public void testIsTestClass() throws IOException {
        classLoader.loadTestClasses();

        Assertions.assertTrue(classLoader.isTestClass("at.unterhuber.test.subpackage.Test3"));
    }

    @Test
    public void testIsNotTestClass() throws IOException {
        classLoader.loadTestClasses();

        Assertions.assertFalse(classLoader.isTestClass("at.unterhuber.test.ClassToTest"));
    }

    @Test
    public void testClassLoader() throws IOException {
        Set<URL> expected = Set.of(
                new URL("file://" + ROOT + "/build/classes/java/test/"),
                new URL("file://" + ROOT + "/build/classes/java/main/"),
                new URL("file://" + ROOT + "/build/classes/java/")
        );

        URLClassLoader loader = (URLClassLoader) classLoader.getClassLoader();

        Assertions.assertEquals(expected, Arrays.stream(loader.getURLs()).collect(Collectors.toSet()));
    }

    @Test
    public void testClassLoaderIsThreadClassLoader() {
        ClassLoader loader = classLoader.getClassLoader();

        Assertions.assertEquals(Thread.currentThread().getContextClassLoader(), loader);
    }

    @AfterEach
    public void cleanup() {
        classLoader = null;
    }
}
