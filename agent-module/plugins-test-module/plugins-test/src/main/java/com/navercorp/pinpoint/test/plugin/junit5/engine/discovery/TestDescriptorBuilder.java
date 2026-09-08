package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import com.navercorp.pinpoint.test.plugin.maven.DependencyResolverFactory;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.engine.TestDescriptor;

import java.util.function.Supplier;

import java.util.function.Predicate;

public interface TestDescriptorBuilder extends Predicate<Class<?>>  {
    /**
     * @param resolverFactory resolves the maven dependencies declared on the test class; owned by the engine and
     *                        created on the first {@code get()}, so builders that resolve nothing must not call it
     */
    TestDescriptor build(TestDescriptor testDescriptor, Class<?> testClass, JupiterConfiguration configuration, Supplier<DependencyResolverFactory> resolverFactory);
}
