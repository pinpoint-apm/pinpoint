package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.engine.TestDescriptor;

import java.util.function.Predicate;

public interface TestDescriptorBuilder extends Predicate<Class<?>>  {
    TestDescriptor build(TestDescriptor testDescriptor, JupiterConfiguration configuration);
}
