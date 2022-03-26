package at.unterhuber.test_runner;

import at.unterhuber.test_runner.path.MavenPathHandler;
import at.unterhuber.test_runner.util.FileClassLoader;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.testapi.TestGroupConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MutationTestExecutor {
    public static void main(String[] args) throws IOException {
        boolean runAll = Boolean.parseBoolean(args[0]);
        String packageName = args[1];
        String testsToRunString = Files.readString(Path.of("tests_to_run.txt"));
        String changedClassesString = Files.readString(Path.of("changed_classes.txt"));
        if (!testsToRunString.equals("[]")) {
            String[] changedClasses = changedClassesString.replace("[", "").replace("]", "").split(", ");
            String[] testsToRun = testsToRunString.replace("[", "").replace("]", "").split(", ");
            executeTests(testsToRun, changedClasses, System.getProperty("user.dir"), runAll, packageName);
        } else {
            System.out.println("No tests to run");
        }
        new File("changed_classes.txt").delete();
        new File("tests_to_run.txt").delete();
        new File("mycache").delete();
        System.exit(0);
    }

    public static void executeTests(String[] testsToRun, String[] changedClasses, String rootDirectory, boolean runAll, String packageName) throws IOException {
        List<String> classes = Arrays.stream(changedClasses).collect(Collectors.toList());
        FileClassLoader classLoader = new FileClassLoader(new MavenPathHandler("/home/martin/commons-cli"));
        classLoader.initClassLoader();
        classLoader.loadTestClasses();
        ReportOptions options = new ReportOptions();
        options.setReportDir(rootDirectory + "/target/report");
        options.setGroupConfig(TestGroupConfig.emptyConfig());
        options.setSourceDirs(List.of(new File(rootDirectory + "/src")));
        options.addOutputFormats(List.of("HTML"));
        options.setTimeoutConstant(1000L);
        if (runAll) {
            Set<String> testClassesNames = classLoader.getTestClassesNames();
            classes.addAll(testClassesNames);
        } else {
            classes.addAll(Arrays.stream(testsToRun).collect(Collectors.toList()));
        }
        options.setTargetClasses(classes);
        PluginServices pluginServices = new PluginServices(classLoader.getClassLoader());
        new EntryPoint().execute(new File(rootDirectory), options, pluginServices, Map.of());
    }
}
