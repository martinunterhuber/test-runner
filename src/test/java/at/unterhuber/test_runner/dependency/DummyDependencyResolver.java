package at.unterhuber.test_runner.dependency;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DummyDependencyResolver extends DependencyResolver {
    public DummyDependencyResolver() {
        super(null, null, null);
    }

    @Override
    public Set<String> resolveDependenciesFor(List<String> classes) throws IOException {
        return Set.of("at.unterhuber.test.MyTestClass");
    }
}
