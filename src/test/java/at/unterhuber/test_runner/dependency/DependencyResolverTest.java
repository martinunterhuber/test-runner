package at.unterhuber.test_runner.dependency;

import at.unterhuber.test_runner.path.GradlePathHandler;
import at.unterhuber.test_runner.path.ProjectPathHandler;
import at.unterhuber.test_runner.util.FileClassLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DependencyResolverTest {
    private final String PACKAGE = "at.unterhuber.test";
    private DependencyResolver resolver;

    @BeforeEach
    public void setup() throws IOException, ClassNotFoundException {
        ProjectPathHandler handler = new GradlePathHandler(System.getProperty("user.dir") + "/testProject");
        FileClassLoader cl = new FileClassLoader(handler);
        cl.initClassLoader();
        cl.loadTestClasses();
        resolver = new DependencyResolver(cl, handler, PACKAGE);
    }

    @Test
    public void testResolveDependencies() throws IOException {
        Set<String> expected = Set.of("at.unterhuber.test.ClassToTest", "at.unterhuber.test.subpackage.AnotherClassToTest", "at.unterhuber.test.subpackage.Test3", "at.unterhuber.test.Junit4Test1");
        Assertions.assertEquals(expected, resolver.resolveDependenciesFor(List.of("at.unterhuber.test.ClassToTest")));
    }

    @Test
    public void testResolveDependenciesRecursive() throws IOException {
        Set<String> expected = Set.of("at.unterhuber.test.ClassToTest", "at.unterhuber.test.subpackage.AnotherClassToTest", "at.unterhuber.test.subpackage.Test3", "at.unterhuber.test.Junit4Test1");
        Assertions.assertEquals(expected, resolver.resolveDependenciesFor(List.of("at.unterhuber.test.ClassToTest"), 5));
    }

    @AfterEach
    public void cleanup() {
        resolver = null;
    }
}
