package at.unterhuber.test_runner;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKNotifier;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class Runner {
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

    public static void main(String[] args) throws MalformedURLException {
        Map<String, CKClassResult> metrics = getMetrics(args[0]);

        for(Map.Entry<String, CKClassResult> entry : metrics.entrySet()){
            System.out.println(entry.getValue().getClassName() + ": " + entry.getValue().getNumberOfMethods());
        }

        File file = new File(args[0] + "/build/classes/java/test");
        URL[] urls = new URL[]{file.toURI().toURL()};
        ClassLoader cl = new URLClassLoader(urls);

        Thread.currentThread().setContextClassLoader(cl);
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage(args[1])
                )
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
    }
}
