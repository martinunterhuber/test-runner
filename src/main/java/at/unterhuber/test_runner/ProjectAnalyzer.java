package at.unterhuber.test_runner;

import at.unterhuber.test_runner.bug.Bug;
import at.unterhuber.test_runner.bug.BugsMeasure;
import at.unterhuber.test_runner.dependency.DependencyResolver;
import at.unterhuber.test_runner.git.Apriori;
import at.unterhuber.test_runner.git.GitParser;
import at.unterhuber.test_runner.git.GitStats;
import at.unterhuber.test_runner.issue.Issue;
import at.unterhuber.test_runner.issue.IssueMeasure;
import at.unterhuber.test_runner.metric.Metric;
import at.unterhuber.test_runner.metric.MetricMeasure;
import at.unterhuber.test_runner.metric.RiskCalculator;
import at.unterhuber.test_runner.path.GradlePathHandler;
import at.unterhuber.test_runner.path.MavenPathHandler;
import at.unterhuber.test_runner.path.ProjectPathHandler;
import at.unterhuber.test_runner.test.TestSelector;
import at.unterhuber.test_runner.util.Config;
import at.unterhuber.test_runner.util.FileClassLoader;
import edu.umd.cs.findbugs.Priorities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class ProjectAnalyzer {
    // TODO: add these to config?
    private static final String[] metricNames = new String[]{
            "Loc",
            "Wmc",
            "Rfc",
            "Cbo",
            "Dit",
            "Noc",
            "Fanin",
            "Fanout",
            "NumberOfMethods",
            "NumberOfFields"
    };
    private static final String[] testMetricNames = new String[]{
            "Wmc",
            "NumberOfMethods"
    };

    public static void main(String[] args) throws Throwable {
        List<String> testsToRun = analyzeProject(args[0], args[1], args[2], args[3], args[4]);
        Files.writeString(Path.of("tests_to_run.txt"), testsToRun.toString());
    }

    public static List<String> analyzeProject(String projectRoot, String packageName, String selfRootPath, String commit, String root) throws IOException, InterruptedException {
        Metric[] metrics = Arrays.stream(metricNames).map(Metric::new).toArray(Metric[]::new);
        Metric[] testMetrics = Arrays.stream(testMetricNames).map(Metric::new).toArray(Metric[]::new);

        ProjectPathHandler pathHandler;
        if (Files.exists(Path.of(projectRoot).resolve("target"))) {
            System.out.println("Inferred Build Tool: Maven\n");
            pathHandler = new MavenPathHandler(projectRoot);
        } else if (Files.exists(Path.of(projectRoot).resolve("build"))) {
            System.out.println("Inferred Build Tool: Gradle\n");
            pathHandler = new GradlePathHandler(projectRoot);
        } else {
            System.out.println("Skipping " + projectRoot + ": build directory is missing (did you forget to compile the program?)\n");
            return Collections.emptyList();
        }
        if (!pathHandler.getTestClassPath().toFile().exists()
                || !pathHandler.getMainClassPath().toFile().exists()
                || !pathHandler.getTestSourcePath().toFile().exists()
                || !pathHandler.getMainSourcePath().toFile().exists()) {
            System.out.println("Skipping " + projectRoot + ": project is empty\n");
            return Collections.emptyList();
        }

        Config config = new Config(pathHandler.getRootPath(), Path.of(selfRootPath));
        FileClassLoader loader = new FileClassLoader(pathHandler);
        DependencyResolver resolver = new DependencyResolver(loader, pathHandler, packageName);
        TestSelector selector = new TestSelector(loader, config, resolver);
        MetricMeasure measure = new MetricMeasure(pathHandler.getMainSourcePath().toString(), metrics);
        MetricMeasure testMeasure = new MetricMeasure(pathHandler.getTestSourcePath().toString(), testMetrics);
        RiskCalculator calculator = new RiskCalculator(measure, config);
        RiskCalculator testCalculator = new RiskCalculator(testMeasure, config);
        IssueMeasure issueMeasure = new IssueMeasure(pathHandler);
        BugsMeasure bugsMeasure = new BugsMeasure(pathHandler, loader, Priorities.NORMAL_PRIORITY);
        GitParser gitParser = new GitParser(pathHandler);

        boolean scanAll = commit == null || commit.isBlank();
        List<String> changedFiles = new ArrayList<>();
        if (!scanAll) {
            changedFiles = GitParser.getDiffClasses(commit)
                    .stream()
                    .map((path) -> root + "/" + path)
                    .map(pathHandler::pathToFullClassName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Files.writeString(Path.of("changed_classes.txt"), changedFiles.toString());
            if (changedFiles.size() == 0) {
                System.out.println("Skipping " + projectRoot + ": no files were changed\n");
                return Collections.emptyList();
            }
            System.out.println("Changed files\n" + changedFiles + "\n");
        }

        config.loadConfigFromFile();

        loader.initClassLoader();
        loader.loadTestClasses();

        Thread[] threads = new Thread[5];
        threads[0] = new Thread(() -> {
            measure.measure();
            testMeasure.measure();
            try {
                measure.initMeasurements();
                testMeasure.initMeasurements();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        threads[1] = new Thread(() -> {
            try {
                issueMeasure.findIssues();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        threads[2] = new Thread(() -> {
            try {
                bugsMeasure.findBugs();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        threads[3] = new Thread(() -> {
        });
        if (!scanAll) {
            List<String> finalChangedFiles = changedFiles;
            threads[3] = new Thread(() -> {
                try {
                    selector.determineChangeSet(finalChangedFiles);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        AtomicReference<List<Apriori.Combination<String>>> combinations = new AtomicReference<>();
        threads[4] = new Thread(() -> {
            try {
                gitParser.parseLog();
                GitStats stats = gitParser.getStats();
                combinations.set(stats.findFilesOftenChangedTogether());
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

        calculator.setCombinations(combinations.get());
        calculator.setStats(gitParser.getStats());
        testCalculator.setCombinations(combinations.get());
        testCalculator.setStats(gitParser.getStats());
        HashMap<String, Double> risk = calculator.getRiskByClass();
        HashMap<String, Double> testRisk = testCalculator.getRiskByClass();

        selector.determineClassesToTest(risk, issues, bugs);
        selector.determineTestsToRun(testRisk, testIssues, testBugs);

        return selector.selectTestClasses();
    }
}
