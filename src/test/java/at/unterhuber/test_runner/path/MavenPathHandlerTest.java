package at.unterhuber.test_runner.path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class MavenPathHandlerTest {
    private final String ROOT = "/my/test/dir";
    private MavenPathHandler pathHandler;

    @BeforeEach
    public void setup() {
        pathHandler = new MavenPathHandler(ROOT);
    }

    @Test
    public void testGetRootPath() {
        Assertions.assertEquals(ROOT, pathHandler.getRootPath().toString());
    }

    @Test
    public void testGetSourcePath() {
        Assertions.assertEquals(ROOT + "/src", pathHandler.getSourcePath().toString());
    }

    @Test
    public void testGetClassPath() {
        Assertions.assertEquals(ROOT + "/target", pathHandler.getClassPath().toString());
    }

    @Test
    public void testGetMainSourcePath() {
        Assertions.assertEquals(ROOT + "/src/main/java", pathHandler.getMainSourcePath().toString());
    }

    @Test
    public void testGetMainClassPath() {
        Assertions.assertEquals(ROOT + "/target/classes", pathHandler.getMainClassPath().toString());
    }

    @Test
    public void testGetTestSourcePath() {
        Assertions.assertEquals(ROOT + "/src/test/java", pathHandler.getTestSourcePath().toString());
    }

    @Test
    public void testGetTestClassPath() {
        Assertions.assertEquals(ROOT + "/target/test-classes", pathHandler.getTestClassPath().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ROOT + "/src/main/java/C1.java",
            ROOT + "/src/main/java/my/package/C2.java",
            ROOT + "/src/main/java/my/package/subpackage/C3.java",
    })
    public void testIsMainSourcePath_returnsTrue(String path) {
        Assertions.assertTrue(pathHandler.isMainSourcePath(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ROOT + "/src/main/C1.java",
            ROOT + "/src/test/java/my/package/T2.java",
            ROOT + "/classes/my/package/subpackage/C3.class",
            "/not/root/src/main/java/my/package/subpackage/C4.java",
            "src/main/java/C5.java"
    })
    public void testIsMainSourcePath_returnsFalse(String path) {
        Assertions.assertFalse(pathHandler.isMainSourcePath(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ROOT + "/src/test/java/T1.java",
            ROOT + "/src/test/java/my/package/T2.java",
            ROOT + "/src/test/java/my/package/subpackage/T3.java",
    })
    public void testIsTestSourcePath_returnsTrue(String path) {
        Assertions.assertTrue(pathHandler.isTestSourcePath(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ROOT + "/src/test/T1.java",
            ROOT + "/src/main/java/my/package/C2.java",
            ROOT + "/test-classes/my/package/subpackage/T3.class",
            "/not/root/src/test/java/my/package/subpackage/T4.java",
            "src/test/java/T5.java"
    })
    public void testIsTestSourcePath_returnsFalse(String path) {
        Assertions.assertFalse(pathHandler.isTestSourcePath(path));
    }

    @ParameterizedTest
    @CsvSource({
            "C1,src/main/java/C1.java",
            "my.package.C2,src/main/java/my/package/C2.java",
            "my.package.T3,src/test/java/my/package/T3.java",
            "my.package.C4,target/classes/my/package/C4.class",
            "my.package.T5,target/test-classes/my/package/T5.class",
    })
    public void testPathToFullClassName_validPaths(String expected, String path) {
        Assertions.assertEquals(expected, pathHandler.pathToFullClassName(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/asdf/java/C1.java",
            "test/T2.java"
    })
    public void testPathToFullClassName_invalidPathsReturnNull(String path) {
        Assertions.assertNull(pathHandler.pathToFullClassName(path));
    }

    @AfterEach
    public void cleanup() {
        pathHandler = null;
    }
}
