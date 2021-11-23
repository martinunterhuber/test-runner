package at.unterhuber.test_runner;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class Runner {
    public static void main(String[] args) throws MalformedURLException {
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
        System.out.println(summary.getTestsStartedCount());


//        Map<String, CKClassResult> results = new HashMap<>();
//
//        new CK().calculate("/home/martin/Webcrawler-CleanCode", new CKNotifier() {
//            @Override
//            public void notify(CKClassResult result) {
//
//                // Store the metrics values from each component of the project in a HashMap
//                results.put(result.getClassName(), result);
//
//            }
//
//            @Override
//            public void notifyError(String sourceFilePath, Exception e) {
//                System.err.println("Error in " + sourceFilePath);
//                e.printStackTrace(System.err);
//            }
//        });
//
//        // Write the metrics value of each component in the csv files
//        for(Map.Entry<String, CKClassResult> entry : results.entrySet()){
//            System.out.println(entry.getValue().getClassName() + ": " + entry.getValue().getNumberOfMethods());
//        }
//
//        System.out.println("Metrics extracted!!!");
    }
}
