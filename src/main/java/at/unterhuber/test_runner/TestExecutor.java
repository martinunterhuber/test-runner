package at.unterhuber.test_runner;

import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class TestExecutor {
    public static void main(String[] args) throws IOException {
        String testsToRunString = Files.readString(Path.of("tests_to_run.txt"));
        if (!testsToRunString.equals("[]")) {
            String[] testsToRun = testsToRunString.replace("[", "").replace("]", "").split(", ");
            executeTests(testsToRun);
        }
    }

    public static void executeTests(String[] testsToRun) {
        List<ClassSelector> selectors = Arrays.stream(testsToRun)
                .map(DiscoverySelectors::selectClass)
                .collect(Collectors.toList());

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors)
                .filters(includeClassNamePatterns(".*"))
                .build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        try (LauncherSession session = LauncherFactory.openSession()) {
            Launcher launcher = session.getLauncher();
            launcher.registerTestExecutionListeners(listener);
            launcher.execute(request);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));

        if (summary.getTestsFailedCount() > 0) {
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                System.out.println(failure.getTestIdentifier());
                failure.getException().printStackTrace(System.out);
                System.out.println();
            }
            System.out.println(summary.getTestsStartedCount() + " tests started, " + summary.getTestsFailedCount() + " failed");
            throw new RuntimeException("Some tests failed!");
        }
    }
}
