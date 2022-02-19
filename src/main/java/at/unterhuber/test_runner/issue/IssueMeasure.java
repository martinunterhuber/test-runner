package at.unterhuber.test_runner.issue;

import at.unterhuber.test_runner.path.ProjectPathHandler;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.util.datasource.DataSource;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class IssueMeasure {
    private final ProjectPathHandler pathHandler;
    private final Map<String, List<Issue>> issues = new HashMap<>();
    private final Map<String, List<Issue>> testIssues = new HashMap<>();

    public IssueMeasure(ProjectPathHandler pathHandler) {
        this.pathHandler = pathHandler;
    }

    public void findIssues() throws IOException {
        Report report = generateReport();
        for (RuleViolation violation : report.getViolations()) {
            Map<String, List<Issue>> issuesByClass;
            if (pathHandler.isMainSourcePath(violation.getFilename())) {
                issuesByClass = issues;
            } else {
                issuesByClass = testIssues;
            }
            List<Issue> issuesList = issuesByClass.getOrDefault(violation.getFilename(), new ArrayList<>());
            issuesList.add(new Issue(violation));
            issuesByClass.put(violation.getFilename(), issuesList);
        }
        printIssues(issues);
        printIssues(testIssues);
    }

    public void printIssues(Map<String, List<Issue>> issues) {
        System.out.println("Issues\n" + toLineSeparatedString(issues.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(pathHandler.pathToFullClassName(entry.getKey()), entry.getValue()
                        .stream()
                        .map(Issue::computeRisk)
                        .reduce(Integer::sum)
                        .orElse(0)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))) + "\n");
    }

    private Report generateReport() throws IOException {
        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setMinimumPriority(RulePriority.LOW);
        configuration.setInputPaths(pathHandler.getSourcePath().toString());
        configuration.prependClasspath(pathHandler.getClassPath().toString());
        configuration.setRuleSets("rulesets/java/quickstart.xml");

        // TODO: check if this works for github actions
        // Use git diff?
        configuration.setAnalysisCacheLocation(pathHandler.getRootPath().toString() + "/mycache");
        RuleSetLoader ruleSetLoader = RuleSetLoader.fromPmdConfig(configuration);
        List<RuleSet> ruleSets = ruleSetLoader.loadFromResources(Arrays.asList(configuration.getRuleSets().split(",")));
        List<DataSource> files = PMD.getApplicableFiles(configuration, Collections.singleton(LanguageRegistry.getDefaultLanguage()));
        try {
            Logger.getLogger("").setLevel(Level.WARNING);
            return PMD.processFiles(configuration, ruleSets, files, Collections.emptyList());
        } finally {
            ClassLoader auxiliaryClassLoader = configuration.getClassLoader();
            if (auxiliaryClassLoader instanceof URLClassLoader) {
                ((URLClassLoader) auxiliaryClassLoader).close();
            }
        }
    }

    public Map<String, List<Issue>> getIssues() {
        return issues;
    }

    public Map<String, List<Issue>> getTestIssues() {
        return testIssues;
    }
}
