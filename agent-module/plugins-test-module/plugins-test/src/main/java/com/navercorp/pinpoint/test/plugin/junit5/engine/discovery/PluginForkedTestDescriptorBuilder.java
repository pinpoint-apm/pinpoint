package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import com.navercorp.pinpoint.test.plugin.DefaultPluginForkedTestSuite;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.PluginForkedTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestUnitTestDescriptor;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.engine.TestDescriptor;

import java.lang.reflect.Method;
import java.util.List;

public class PluginForkedTestDescriptorBuilder implements TestDescriptorBuilder {

    @Override
    public boolean test(Class<?> candidate) {
        return AnnotationUtils.isAnnotated(candidate, PluginForkedTest.class);
    }

    public TestDescriptor build(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
        final DefaultPluginForkedTestSuite testSuite = new DefaultPluginForkedTestSuite(((ClassTestDescriptor) testDescriptor).getTestClass());
        final List<PluginForkedTestInstance> testInstanceList = testSuite.getPluginTestInstanceList();
        final TestDescriptorFactory factory = new TestDescriptorFactory(configuration);

        // Unit
        final PluginForkedTestUnitTestDescriptor pluginTestUnitTestDescriptor = new PluginForkedTestUnitTestDescriptor(testDescriptor.getUniqueId(), ((ClassTestDescriptor) testDescriptor).getTestClass(), configuration, testInstanceList);
        for (PluginForkedTestInstance pluginTestInstance : testInstanceList) {
            final String testId = pluginTestInstance.getTestId();
            // Dependency
            final PluginForkedTestDependencyTestDescriptor pluginTestDependencyTestDescriptor = factory.newPluginForkedTestDependencyTestDescriptor(pluginTestUnitTestDescriptor, testId);
            if (pluginTestDependencyTestDescriptor != null) {
                pluginTestUnitTestDescriptor.addChild(pluginTestDependencyTestDescriptor);
                // Class
                final PluginForkedTestClassTestDescriptor pluginTestClassTestDescriptor = factory.newPluginForkedTestClassTestDescriptor(pluginTestDependencyTestDescriptor);
                if (pluginTestClassTestDescriptor != null) {
                    pluginTestDependencyTestDescriptor.addChild(pluginTestClassTestDescriptor);
                    for (TestDescriptor descriptor : testDescriptor.getChildren()) {
                        if (descriptor instanceof TestMethodTestDescriptor) {
                            final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                            final PluginForkedTestMethodTestDescriptor pluginTestMethodTestDescriptor = factory.newPluginForkedTestMethodTestDescriptor(pluginTestClassTestDescriptor, pluginTestInstance, method);
                            pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
                        }
                    }
                }
            }
        }
        return pluginTestUnitTestDescriptor;
    }
}
