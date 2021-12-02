package at.unterhuber.test_runner;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class TestExecutor {
    private final TestSelector selector;

    public TestExecutor(TestSelector selector) {
        this.selector = selector;
    }

    public void executeTests() throws Throwable {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selector.selectTestClasses())
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

        if (summary.getTestsFailedCount() > 0) {
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                failure.getException().printStackTrace();
                System.err.println();
            }
            System.err.println(summary.getTestsStartedCount() + " tests started, " + summary.getTestsFailedCount() + " failed");
            throw new RuntimeException("Some tests failed!");
        }
    }
}
