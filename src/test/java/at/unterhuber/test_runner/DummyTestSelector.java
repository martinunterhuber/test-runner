package at.unterhuber.test_runner;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.util.List;

public class DummyTestSelector extends TestSelector {
    public DummyTestSelector() {
        super(null, null, null);
    }

    @Override
    public List<DiscoverySelector> selectTestClasses() {
        return List.of(DiscoverySelectors.selectClass("at.unterhuber.test.Test2"));
    }
}
