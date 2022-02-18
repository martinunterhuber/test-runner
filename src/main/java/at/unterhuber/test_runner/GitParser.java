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
    private Map<String, String> renameMap;
    private GitStats stats;

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
        renameMap = new HashMap<>();
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
                if (parts[0].equals("-") || parts.length <= 2 || !parts[2].endsWith(".java")) {
                    continue;
                }
                String path = handleRename(parts[2]);
                String clazz = pathHandler.getFullClassNameFrom(Path.of(path));
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

    public GitStats getStats() {
        if (stats == null) {
            stats = new GitStats(commits, reverseIdMap);
            stats.initStats();
        }
        return stats;
    }

    private String handleRename(String path) {
        if (path.contains(" => ")) {
            String[] pathParts = path.split("[{}]");
            String oldPath, newPath;
            if (pathParts.length > 1) {
                String[] rename = pathParts[1].split(" => ");
                oldPath = pathParts[0] + rename[0] + pathParts[2];
                newPath = pathParts[0] + rename[1] + pathParts[2];
            } else {
                String[] rename = pathParts[0].split(" => ");
                oldPath = rename[0];
                newPath = rename[1];
            }
            oldPath = oldPath.replace("//", "/");
            newPath = newPath.replace("//", "/");
            renameMap.put(oldPath, newPath);
            path = newPath;
        }
        while (renameMap.containsKey(path)) {
            path = renameMap.get(path);
        }
        return path;
    }

    public List<GitCommit> getCommits() {
        return commits;
    }

    public Map<Integer, String> getReverseIdMap() {
        return reverseIdMap;
    }
}
