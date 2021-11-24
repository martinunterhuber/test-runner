package at.unterhuber.test_runner;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;


public class Runner {
    public static ClassLoader initClassLoader(String path) throws MalformedURLException {
        File testDir = new File(path + "build/classes/java/test");
        File mainDir = new File(path + "build/classes/java/main");
        URL[] urls = new URL[]{testDir.toURI().toURL(), mainDir.toURI().toURL()};
        ClassLoader cl = new URLClassLoader(urls);
        Thread.currentThread().setContextClassLoader(cl);
        return cl;
    }

    public static List<DiscoverySelector> selectClasses(FileClassLoader loader) {
        List<Field> fields = loader.getFields();
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
        RiskCalculator calc = new RiskCalculator(path, new RiskMetric[]{new RiskMetric("Cbo"), new RiskMetric("NumberOfMethods")});
        calc.measure();
        calc.printSelectedMetrics();
        ClassLoader cl = initClassLoader(path);
        FileClassLoader loader = new FileClassLoader(path, packageName, cl);
        loader.loadClasses();
        List<DiscoverySelector> selectors = selectClasses(loader);
        executeTests(selectors);
    }
}
