package com.navercorp.pinpoint.test.plugin.shared;

import java.util.Objects;
import java.util.Properties;

/**
 * @author emeroad
 */
public class SharedTestLifeCycleWrapper {
    private final SharedTestLifeCycle sharedTestLifecycle;
    private Properties lifeCycleResult;

    private static Class<? extends SharedTestLifeCycle> getSharedTestLifeCycle(final Class<?> testClazz) {
        SharedTestLifeCycleClass sharedTestLifeCycleClass = testClazz.getAnnotation(SharedTestLifeCycleClass.class);
        if (sharedTestLifeCycleClass == null) {
            return null;
        }
        return sharedTestLifeCycleClass.value();
    }

    public static SharedTestLifeCycleWrapper newSharedTestLifeCycleWrapper(final Class<?> testClazz) {
        Class<? extends SharedTestLifeCycle> versionTestClazz = getSharedTestLifeCycle(testClazz);
        if (versionTestClazz == null) {
            return null;
        }
        try {
            return new SharedTestLifeCycleWrapper(versionTestClazz.newInstance());
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
