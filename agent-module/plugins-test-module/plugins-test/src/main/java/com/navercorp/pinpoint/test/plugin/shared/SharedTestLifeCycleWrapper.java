package com.navercorp.pinpoint.test.plugin.shared;

import java.util.Objects;
import java.util.Properties;

/**
 * @author emeroad
 */
public class SharedTestLifeCycleWrapper {
    private final SharedTestLifeCycle sharedTestLifecycle;
    private Properties lifeCycleResult;

    public static SharedTestLifeCycleWrapper newSharedTestLifeCycleWrapper(final Class<?> testClazz) {
        try {
            return new SharedTestLifeCycleWrapper((SharedTestLifeCycle) testClazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public SharedTestLifeCycleWrapper(SharedTestLifeCycle sharedTestLifecycle) {
        this.sharedTestLifecycle = Objects.requireNonNull(sharedTestLifecycle, "versionTestLifecycle");
    }

    public Properties getLifeCycleResult() {
        return lifeCycleResult;
    }

    public void beforeAll() {
        lifeCycleResult = sharedTestLifecycle.beforeAll();
    }

    public void afterAll() {
        sharedTestLifecycle.afterAll();
    }
}
