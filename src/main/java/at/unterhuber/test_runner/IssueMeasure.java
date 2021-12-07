package at.unterhuber.test_runner;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.util.datasource.DataSource;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;

public class IssueMeasure {
    private final ProjectPathHandler pathHandler;
    private Map<String, List<Issue>> issues = new HashMap<>();
    private Map<String, List<Issue>> testIssues = new HashMap<>();

    public IssueMeasure(ProjectPathHandler pathHandler) {
        this.pathHandler = pathHandler;
    }

    public void initIssuesByClass() throws IOException {
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