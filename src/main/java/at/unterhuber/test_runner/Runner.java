package at.unterhuber.test_runner;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKNotifier;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;


public class Runner {
    private static final String[] METRICS = new String[]{
            "NumberOfMethods",
            "Cbo",
    };

    public static Map<String, CKClassResult> getMetrics(String path) {
        Map<String, CKClassResult> results = new HashMap<>();

        new CK().calculate(path + "src/main/", new CKNotifier() {
            @Override
            public void notify(CKClassResult result) {
                results.put(result.getClassName(), result);
            }

            @Override
            public void notifyError(String sourceFilePath, Exception e) {
                System.err.println("Error in " + sourceFilePath);
                e.printStackTrace(System.err);
            }
        });

        return results;
    }

    public static ClassLoader initClassLoader(String path) throws MalformedURLException {
        File testDir = new File(path + "build/classes/java/test");
        File mainDir = new File(path + "build/classes/java/main");
        URL[] urls = new URL[]{testDir.toURI().toURL(), mainDir.toURI().toURL()};
        ClassLoader cl = new URLClassLoader(urls);
        Thread.currentThread().setContextClassLoader(cl);
        return cl;
    }

    public static void printSelectedMetrics(Map<String, CKClassResult> metrics) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for(Map.Entry<String, CKClassResult> entry : metrics.entrySet()) {
            for (String metric : METRICS) {
                Method method = CKClassResult.class.getMethod("get" + metric);
                System.out.println(entry.getKey() + " - " + metric + ": " + method.invoke(entry.getValue()));
            }
        }
    }

    public static List<DiscoverySelector> selectClasses(String path, String packageName, ClassLoader cl) throws IOException {
        List<Class<?>> testClasses = Files
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

        List<Field> fields = testClasses
                .stream()
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredFields()))
                .collect(Collectors.toList());

        List<DiscoverySelector> selectors = new ArrayList<>();
        for (Field field : fields) {
            selectors.add(selectClass(field.getDeclaringClass()));
        }
        return selectors;
    }

    public static void executeTests(List<DiscoverySelector> selectors) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors)
                .filters(includeClassNamePatterns(".*"))
                .build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        try (LauncherSession session = LauncherFactory.openSession()) {
            Launcher launcher = session.getLauncher();
            launcher.registerTestExecutionListeners(listener);
            TestPlan testPlan = launcher.discover(request);
            launcher.execute(testPlan);
            launcher.execute(request);
        }

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));
        summary.printFailuresTo(new PrintWriter(System.out));

        if (summary.getTestsFailedCount() > 0) {
            throw new RuntimeException("Failed");
        }
    }

    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String path = args[0];
        String packageName = args[1];
        Map<String, CKClassResult> metrics = getMetrics(path);
        printSelectedMetrics(metrics);
        ClassLoader cl = initClassLoader(path);
        List<DiscoverySelector> selectors = selectClasses(path, packageName, cl);
        executeTests(selectors);
    }
}
