package at.unterhuber.test_runner.test;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.util.List;

public class DummyTestSelector extends TestSelector {
    public DummyTestSelector() {
        super(null, null, null);
    }

    @Override
    public List<String> selectTestClasses() {
        return List.of("at.unterhuber.test.Test2");
    }
}
