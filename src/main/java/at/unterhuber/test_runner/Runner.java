package at.unterhuber.test_runner;

import edu.umd.cs.findbugs.Priorities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class Runner {
    // TODO: add these to config?
    private static final String[] metricNames = new String[]{
            "Loc",
            "Wmc",
            "Rfc",
            "Cbo",
            "Dit",
            "NumberOfMethods",
            "NumberOfFields"
    };
    private static final String[] testMetricNames = new String[]{
            "Wmc",
            "NumberOfMethods"
    };

    public static void main(String[] args) throws Throwable {
        String rootPath = args[0];
        String packageName = args[1];
        String selfRootPath = args[2];

        Metric[] metrics = Arrays.stream(metricNames).map(Metric::new).toArray(Metric[]::new);
        Metric[] testMetrics = Arrays.stream(testMetricNames).map(Metric::new).toArray(Metric[]::new);

        ProjectPathHandler pathHandler;
        if (Files.exists(Path.of(rootPath).resolve("target"))) {
            System.out.println("Inferred Build Tool: Maven\n");
            pathHandler = new MavenPathHandler(rootPath);
        } else {
            System.out.println("Inferred Build Tool: Gradle\n");
            pathHandler = new GradlePathHandler(rootPath);
        }
        Config config = new Config(pathHandler.getRootPath(), Path.of(selfRootPath));
        FileClassLoader loader = new FileClassLoader(pathHandler);
        DependencyResolver resolver = new DependencyResolver(loader, pathHandler, packageName);
        TestSelector selector = new TestSelector(loader, config, resolver);
        TestExecutor executor = new TestExecutor(selector);
        MetricMeasure measure = new MetricMeasure(pathHandler.getMainSourcePath().toString(), metrics);
        MetricMeasure testMeasure = new MetricMeasure(pathHandler.getTestSourcePath().toString(), testMetrics);
        RiskCalculator calculator = new RiskCalculator(measure, config);
        RiskCalculator testCalculator = new RiskCalculator(testMeasure, config);
        IssueMeasure issueMeasure = new IssueMeasure(pathHandler);
        BugsMeasure bugsMeasure = new BugsMeasure(pathHandler, loader, Priorities.NORMAL_PRIORITY);
        GitParser gitParser = new GitParser(pathHandler);

        String diff = System.getenv("DIFF");
        boolean scanAll = diff == null;
        List<String> changedFiles = new ArrayList<>();
        if (!scanAll) {
            changedFiles = Arrays
                    .stream(diff.split(" "))
                    .map(pathHandler::pathToFullClassName)
                    .collect(Collectors.toList());
            System.out.println("Changed files\n" + changedFiles + "\n");
        }

        config.loadConfigFromFile();

        loader.initClassLoader();
        loader.loadTestClasses();

        Thread[] threads = new Thread[6];
        threads[0] = new Thread(() -> {
            measure.measure();
            try {
                measure.initMeasurements();
                // measure.printSelectedMetrics();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        threads[1] = new Thread(() -> {
            testMeasure.measure();
            try {
                testMeasure.initMeasurements();
                // testMeasure.printSelectedMetrics();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        threads[2] = new Thread(() -> {
            try {
                issueMeasure.findIssues();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        threads[3] = new Thread(() -> {
            try {
                bugsMeasure.findBugs();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        threads[4] = new Thread(() -> {});
        if (!scanAll) {
            List<String> finalChangedFiles = changedFiles;
            threads[4] = new Thread(() -> {
                try {
                    selector.determineChangeSet(finalChangedFiles);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        threads[5] = new Thread(() -> {
            try {
                gitParser.parseLog();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        Map<String, List<Issue>> issues = issueMeasure.getIssues();
        Map<String, List<Issue>> testIssues = issueMeasure.getTestIssues();

        Map<String, List<Bug>> bugs = bugsMeasure.getBugs();
        Map<String, List<Bug>> testBugs = bugsMeasure.getTestBugs();

        HashMap<String, Double> risk = calculator.getRiskByClass();
        HashMap<String, Double> testRisk = testCalculator.getRiskByClass();
        List<GitCommit> commits = gitParser.getCommits();

        selector.determineClassesToTest(risk, issues, bugs);
        selector.determineTestsToRun(testRisk, testIssues, testBugs);

        executor.executeTests();
    }
}
