package at.unterhuber.test_runner.dependency;

import at.unterhuber.test_runner.path.ProjectPathHandler;
import at.unterhuber.test_runner.util.FileClassLoader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static at.unterhuber.test_runner.util.CollectionFormatter.toLineSeparatedString;

public class DependencyResolver {
    private final FileClassLoader loader;
    private final ProjectPathHandler pathHandler;
    private final String packageName;
    private Graph<String, DefaultEdge> dependencyGraph;

    public DependencyResolver(FileClassLoader loader, ProjectPathHandler pathHandler, String packageName) {
        this.loader = loader;
        this.pathHandler = pathHandler;
        this.packageName = packageName;
    }

    public Set<String> resolveDependenciesFor(List<String> classes) throws IOException {
        if (dependencyGraph == null) {
            List<String> classFilePaths = getAllClassFilePaths();
            buildDependencyGraph(classFilePaths);
        }

        Set<String> visited = new HashSet<>();
        Queue<String> toVisit = new ArrayDeque<>(classes);

        while (!toVisit.isEmpty()) {
            String current = toVisit.remove();
            visited.add(current);
            if (!dependencyGraph.containsVertex(current)) {
                continue;
            }
            for (DefaultEdge edge : dependencyGraph.outgoingEdgesOf(current)) {
                String target = dependencyGraph.getEdgeTarget(edge);
                if (!visited.contains(target)) {
                    toVisit.add(target);
                }
            }
        }
        System.out.println("Changed+dependent classes\n" + toLineSeparatedString(visited) + "\n");

        return visited;
    }

    public Set<String> resolveDependenciesFor(List<String> classes, int maxDepth) throws IOException {
        if (dependencyGraph == null) {
            List<String> classFilePaths = getAllClassFilePaths();
            buildDependencyGraph(classFilePaths);
        }

        Set<String> visited = new HashSet<>();

        for (String clazz : classes) {
            visited.add(clazz);
            try {
                resolveDependenciesRecursive(clazz, visited, 0, maxDepth);
            } catch (Exception e) {
                // e.printStackTrace();
            }

        }
        System.out.println("Changed+dependent classes\n" + toLineSeparatedString(visited) + "\n");

        return visited;
    }

    private void resolveDependenciesRecursive(String clazz, Set<String> visited, int depth, int maxDepth) {
        if (maxDepth <= depth) {
            return;
        }
        if (!dependencyGraph.containsVertex(clazz)) {
            return;
        }
        for (DefaultEdge edge : dependencyGraph.outgoingEdgesOf(clazz)) {
            String target = dependencyGraph.getEdgeTarget(edge);
            if (!visited.contains(target)) {
                visited.add(target);
                resolveDependenciesRecursive(target, visited, depth + 1, maxDepth);
            }
        }
    }

    private void buildDependencyGraph(List<String> classFilePaths) throws IOException {
        dependencyGraph = new SimpleDirectedGraph<>(DefaultEdge.class);

        for (String classFilePath : classFilePaths) {
            DependencyVisitor visitor = new DependencyVisitor();
            ClassReader classReader = new ClassReader(Objects.requireNonNull(loader.getClassLoader().getResourceAsStream(classFilePath)));
            classReader.accept(visitor, 0);
            List<String> dependencies = visitor
                    .getPackages()
                    .stream()
                    .filter(name -> name.startsWith(packageName))
                    .collect(Collectors.toList());
            String temp = classFilePath.replace(".class", "").replace("/", ".");
            dependencyGraph.addVertex(temp);
            for (String dependency : dependencies) {
                dependencyGraph.addVertex(dependency);
                if (!temp.equals(dependency)) {
                    dependencyGraph.addEdge(dependency, temp);
                }
            }
        }
    }

    private List<String> getAllClassFilePaths() throws IOException {
        return Files.walk(pathHandler.getClassPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".class"))
                .map(path -> {
                    String relativePath = pathHandler.getMainClassPath().relativize(path).toString();
                    if (relativePath.startsWith("../")) {
                        relativePath = pathHandler.getTestClassPath().relativize(path).toString();
                    }
                    return relativePath;
                }).collect(Collectors.toList());
    }
}
