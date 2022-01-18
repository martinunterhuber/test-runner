package at.unterhuber.test_runner;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.config.UserPreferences;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Objects;

public class BugsMeasure {
    private final ProjectPathHandler pathHandler;
    private final int priority;

    public BugsMeasure(ProjectPathHandler pathHandler, int priority) {
        this.pathHandler = pathHandler;
        this.priority = priority;
    }

    void find() throws IOException, InterruptedException {
        FindBugs2 findBugs = new FindBugs2();
        Project project = new Project();
        BugReporter bugReporter = new BugCollectionBugReporter(project);
        bugReporter.setPriorityThreshold(priority);
        Files.walk(pathHandler.getMainClassPath()).forEach((file) -> project.addFile(file.toFile().getAbsolutePath()));
        findBugs.setProject(project);
        findBugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        findBugs.setBugReporter(bugReporter);
        UserPreferences defaultUserPreferences = UserPreferences.createDefaultUserPreferences();
        defaultUserPreferences.setEffort(UserPreferences.EFFORT_MAX);
        findBugs.setUserPreferences(defaultUserPreferences);
        findBugs.execute();
        System.out.println(Collections.unmodifiableCollection(Objects.requireNonNull(findBugs.getBugReporter().getBugCollection()).getCollection()));
    }
}
