package at.unterhuber.test_runner;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class DependencyResolver {
    private final FileClassLoader loader;
    private final ProjectPathHandler pathHandler;

    public DependencyResolver(FileClassLoader loader, ProjectPathHandler pathHandler) {
        this.loader = loader;
        this.pathHandler = pathHandler;
    }

    public Set<String> resolveDependenciesFor(List<String> classes) throws IOException {
        List<String> classFilePaths = getAllClassFilePaths();
        Graph<String, DefaultEdge> graph = buildDependencyGraph(classFilePaths);

        Set<String> visited = new HashSet<>();
        Queue<String> toVisit = new ArrayDeque<>(classes);

        while(!toVisit.isEmpty()) {
            String current = toVisit.remove();
            visited.add(current);
            for (DefaultEdge edge: graph.outgoingEdgesOf(current)) {
                String target = graph.getEdgeTarget(edge);
                if (!visited.contains(target)) {
                    toVisit.add(target);
                }
            }
        }
        System.out.println("Changed+dependent classes: " + visited);

        return visited;
    }

    private Graph<String, DefaultEdge> buildDependencyGraph(List<String> classFilePaths) throws IOException {
        Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

        for (String classFilePath : classFilePaths) {
            DependencyVisitor visitor = new DependencyVisitor();
            ClassReader classReader = new ClassReader(Objects.requireNonNull(loader.getClassLoader().getResourceAsStream(classFilePath)));
            classReader.accept(visitor, 0);
            List<String> dependencies = visitor
                    .getPackages()
                    .stream()
                    .filter(name -> name.startsWith("at.unterhuber.test"))
                    .toList();
            for (String dependency : dependencies) {
                String temp = classFilePath.replace(".class", "").replace("/", ".");
                graph.addVertex(temp);
                graph.addVertex(dependency);
                if (!temp.equals(dependency)) {
                    graph.addEdge(dependency, temp);
                }
            }
        }
        return graph;
    }

    private List<String> getAllClassFilePaths() throws IOException {
        return Files.walk(pathHandler.getClassPath())
                .filter(Files::isRegularFile)
                .map(path -> {
                    String relativePath = pathHandler.getMainClassPath().relativize(path).toString();
                    if (relativePath.startsWith("../")) {
                        relativePath = pathHandler.getTestClassPath().relativize(path).toString();
                    }
                    return relativePath;
                }).toList();
    }
}
