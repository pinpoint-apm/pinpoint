package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import com.navercorp.pinpoint.test.plugin.PluginForkedTestInstance;
import com.navercorp.pinpoint.test.plugin.PluginTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestUnitTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestUnitTestDescriptor;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.lang.reflect.Method;
import java.util.Objects;

public class TestDescriptorFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JupiterConfiguration configuration;

    public TestDescriptorFactory(JupiterConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    public PluginTestDependencyTestDescriptor newPluginTestDependencyTestDescriptor(PluginTestUnitTestDescriptor parentTestDescriptor, String testId) {
        try {
            final UniqueId childId = childUniqueId(parentTestDescriptor, "dependency", testId);
            return new PluginTestDependencyTestDescriptor(childId, parentTestDescriptor.getTestClass(), configuration, testId);
        } catch (Throwable t) {
            logger.warn(t, () -> "newPluginTestDependencyTestDescriptor error " + t.getMessage());
            return null;
        }
    }

    public PluginForkedTestDependencyTestDescriptor newPluginForkedTestDependencyTestDescriptor(PluginForkedTestUnitTestDescriptor parentTestDescriptor, String testId) {
        try {
            final UniqueId childId = childUniqueId(parentTestDescriptor, "dependency", testId);
            return new PluginForkedTestDependencyTestDescriptor(childId, parentTestDescriptor.getTestClass(), configuration, testId);
        } catch (Throwable t) {
            logger.warn(t, () -> "newPluginForkedTestDependencyTestDescriptor error " + t.getMessage());
            return null;
        }
    }

    public PluginTestClassTestDescriptor newPluginTestClassTestDescriptor(PluginTestDependencyTestDescriptor parentTestDescriptor, PluginTestInstance pluginTestInstance) {
        try {
            final Class<?> testClass = pluginTestInstance.getTestClass();
            final String testId = testClass.getName();
            final UniqueId childId = childUniqueId(parentTestDescriptor, ClassTestDescriptor.SEGMENT_TYPE, testId);
            return new PluginTestClassTestDescriptor(childId, testClass, configuration, pluginTestInstance);
        } catch (Throwable t) {
            logger.warn(t, () -> "newPluginTestClassTestDescriptor error " + t.getMessage());
            return null;
        }
    }

    private UniqueId childUniqueId(AbstractTestDescriptor desc, String segmentType, String testId) {
        return desc.getUniqueId().append(segmentType, testId);
    }

    public PluginForkedTestClassTestDescriptor newPluginForkedTestClassTestDescriptor(PluginForkedTestDependencyTestDescriptor parentTestDescriptor) {
        try {
            final Class<?> testClass = parentTestDescriptor.getTestClass();
            final String testId = testClass.getName();
            final UniqueId childId = childUniqueId(parentTestDescriptor, ClassTestDescriptor.SEGMENT_TYPE, testId);
            return new PluginForkedTestClassTestDescriptor(childId, testClass, configuration);
        } catch (Throwable t) {
            logger.warn(t, () -> "newPluginForkedTestClassTestDescriptor error " + t.getMessage());
            return null;
        }
    }

    public PluginTestMethodTestDescriptor newPluginTestMethodTestDescriptor(PluginTestClassTestDescriptor parentTestDescriptor, PluginTestInstance pluginTestInstance, Method method) {
        try {
            final Class<?> testClass = pluginTestInstance.getTestClass();
            final Method testMethod = ReflectionUtils.findMethod(testClass, method.getName(), method.getParameterTypes()).orElseThrow(() -> new IllegalStateException("not found method"));
            final String testId = testMethod.getName();
            final UniqueId childId = childUniqueId(parentTestDescriptor, TestMethodTestDescriptor.SEGMENT_TYPE, testId);
            return new PluginTestMethodTestDescriptor(childId, testClass, testMethod, configuration, pluginTestInstance);
        } catch (Throwable t) {
            logger.warn(t, () -> "newPluginTestMethodTestDescriptor error " + t.getMessage());
            return null;
        }
    }

    public PluginForkedTestMethodTestDescriptor newPluginForkedTestMethodTestDescriptor(PluginForkedTestClassTestDescriptor parentTestDescriptor, PluginForkedTestInstance pluginTestInstance, Method method) {
        try {
            final Class<?> testClass = parentTestDescriptor.getTestClass();
            final Method testMethod = ReflectionUtils.findMethod(testClass, method.getName(), method.getParameterTypes()).orElseThrow(() -> new IllegalStateException("not found method"));
            final String testId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
            final UniqueId childId = childUniqueId(parentTestDescriptor, TestMethodTestDescriptor.SEGMENT_TYPE, testId);
            return new PluginForkedTestMethodTestDescriptor(childId, testClass, testMethod, configuration, pluginTestInstance);
        } catch (Throwable t) {
            logger.warn(t, () -> "newPluginForkedTestMethodTestDescriptor error " + t.getMessage());
            return null;
        }
    }
}
