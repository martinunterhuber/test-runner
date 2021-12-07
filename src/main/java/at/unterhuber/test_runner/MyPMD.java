package at.unterhuber.test_runner;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.util.datasource.DataSource;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;

public class MyPMD {
    private final ProjectPathHandler pathHandler;

    public MyPMD(ProjectPathHandler pathHandler) {
        this.pathHandler = pathHandler;
    }

    public Map<String, List<Issue>> getIssuesByClass() throws IOException {
        Report report = generateReport();
        Map<String, List<Issue>> issuesByClass = new HashMap<>();
        for (RuleViolation violation : report.getViolations()) {
            List<Issue> issues = issuesByClass.getOrDefault(violation.getClassName(), new ArrayList<>());
            issues.add(new Issue(violation));
            issuesByClass.put(violation.getClassName(), issues);
        }
        return issuesByClass;
    }

    public Report generateReport() throws IOException {
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
}
