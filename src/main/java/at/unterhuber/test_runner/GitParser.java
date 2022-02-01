package at.unterhuber.test_runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GitParser {
    private final ProjectPathHandler pathHandler;
    private final List<GitCommit> commits;
    private Map<String, Integer> idMap;
    private Map<Integer, String> reverseIdMap;

    public GitParser(ProjectPathHandler pathHandler) {
        this.pathHandler = pathHandler;
        this.commits = new ArrayList<>();
    }

    private String getLog() throws InterruptedException, IOException {
        Process process = new ProcessBuilder(
                "git",
                "--no-pager",
                "log",
                "--numstat",
                "--format=%ncommit%n%h%n%aE%n%at"
        ).redirectErrorStream(true).start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = br.lines().collect(Collectors.joining("\n"));
        process.waitFor();
        return result;
    }

    public void parseLog() throws IOException, InterruptedException {
        int clazzId = 0;
        idMap = new HashMap<>();
        String[] commitsString = getLog().split("commit\n");
        for (String commitString : commitsString) {
            String[] lines = commitString.split("\n");
            if (lines.length == 0) {
                continue;
            }
            GitCommit commit = new GitCommit(lines[0], lines[1], Long.parseLong(lines[2]));
            commits.add(commit);
            for (String line : Arrays.stream(lines).skip(4).collect(Collectors.toList())) {
                String[] parts = line.split("\t");
                if (parts[0].equals("-") || !parts[2].endsWith(".java")) {
                    continue;
                }
                // TODO: consider renames
                String clazz = pathHandler.getFullClassNameFrom(Path.of(parts[2]));
                int index = clazz.lastIndexOf("..");
                if (index != -1) {
                    clazz = clazz.substring(index + 2);
                }
                Integer id = idMap.get(clazz);
                if (id == null) {
                    idMap.put(clazz, ++clazzId);
                    id = clazzId;
                }
                commit.addFileChange(clazz, id, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        }
        reverseIdMap = idMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public List<GitCommit> getCommits() {
        return commits;
    }

    public Map<Integer, String> getReverseIdMap() {
        return reverseIdMap;
    }
}
