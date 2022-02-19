package at.unterhuber.test_runner.bug;

import at.unterhuber.test_runner.path.ProjectPathHandler;
import at.unterhuber.test_runner.util.FileClassLoader;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.config.UserPreferences;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class BugsMeasure {
    private final FindBugs2 findBugs2;

    private final ProjectPathHandler pathHandler;
    private final int priority;

    private final Map<String, List<Bug>> bugs = new HashMap<>();
    private final Map<String, List<Bug>> testBugs = new HashMap<>();

    private final FileClassLoader classLoader;

    public BugsMeasure(ProjectPathHandler pathHandler, FileClassLoader loader, int priority) {
        this.pathHandler = pathHandler;
        this.priority = priority;
        this.findBugs2 = new FindBugs2();
        this.classLoader = loader;
    }

    private void executeFindBugs() throws IOException, InterruptedException {
        Project project = new Project();
        BugReporter bugReporter = new BugCollectionBugReporter(project);
        bugReporter.setPriorityThreshold(priority);
        Files.walk(pathHandler.getMainClassPath()).forEach((file) -> project.addFile(file.toFile().getAbsolutePath()));
        Files.walk(pathHandler.getTestClassPath()).forEach((file) -> project.addFile(file.toFile().getAbsolutePath()));
        findBugs2.setProject(project);
        findBugs2.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        findBugs2.setBugReporter(bugReporter);
        UserPreferences defaultUserPreferences = UserPreferences.createDefaultUserPreferences();
        defaultUserPreferences.setEffort(UserPreferences.EFFORT_MAX);
        findBugs2.setUserPreferences(defaultUserPreferences);
        findBugs2.execute();
    }

    public void findBugs() throws IOException, InterruptedException {
        executeFindBugs();
        Collection<BugInstance> collection = Objects.requireNonNull(findBugs2.getBugReporter().getBugCollection()).getCollection();
        for (BugInstance bugInstance : collection) {
            Map<String, List<Bug>> bugsByClass;
            if (classLoader.getTestClassesNames().contains(bugInstance.getPrimaryClass().getClassName())) {
                bugsByClass = testBugs;
            } else {
                bugsByClass = bugs;
            }
            List<Bug> bugList = bugsByClass.getOrDefault(bugInstance.getPrimaryClass().getClassName(), new ArrayList<>());
            bugList.add(new Bug(bugInstance));
            bugsByClass.put(bugInstance.getPrimaryClass().getClassName(), bugList);
        }
        System.out.println();
        printBugs(bugs);
        printBugs(testBugs);
    }

    public void printBugs(Map<String, List<Bug>> bugs) {
        System.out.println("Bugs\n" + toLineSeparatedString(bugs.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()
                        .stream()
                        .map(Bug::computeRisk)
                        .reduce(Integer::sum)
                        .orElse(0)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))) + "\n");
    }

    public Map<String, List<Bug>> getBugs() {
        return bugs;
    }

    public Map<String, List<Bug>> getTestBugs() {
        return testBugs;
    }
}
