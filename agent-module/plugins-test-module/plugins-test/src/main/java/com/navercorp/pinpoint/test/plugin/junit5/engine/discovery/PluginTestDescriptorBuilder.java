package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import com.navercorp.pinpoint.test.plugin.DefaultPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.PluginTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestUnitTestDescriptor;
import com.navercorp.pinpoint.test.plugin.shared.PluginSharedInstance;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.engine.TestDescriptor;

import java.lang.reflect.Method;
import java.util.List;

public class PluginTestDescriptorBuilder implements TestDescriptorBuilder {

    @Override
    public boolean test(Class<?> candidate) {
        return AnnotationUtils.isAnnotated(candidate, PluginTest.class);
    }

    public TestDescriptor build(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
        final DefaultPluginTestSuite testSuite = new DefaultPluginTestSuite(((ClassTestDescriptor) testDescriptor).getTestClass());
        final PluginSharedInstance sharedInstance = testSuite.getPluginSharedInstance();
        final List<PluginTestInstance> testInstanceList = testSuite.getPluginTestInstanceList();
        final TestDescriptorFactory factory = new TestDescriptorFactory(configuration);

        // Unit
        final PluginTestUnitTestDescriptor pluginTestUnitTestDescriptor = new PluginTestUnitTestDescriptor(testDescriptor.getUniqueId(), ((ClassTestDescriptor) testDescriptor).getTestClass(), configuration, sharedInstance);
        for (PluginTestInstance pluginTestInstance : testInstanceList) {
            final String testId = pluginTestInstance.getTestId();
            // Dependency
            final PluginTestDependencyTestDescriptor pluginTestDependencyTestDescriptor = factory.newPluginTestDependencyTestDescriptor(pluginTestUnitTestDescriptor, testId);
            if (pluginTestDependencyTestDescriptor != null) {
                pluginTestUnitTestDescriptor.addChild(pluginTestDependencyTestDescriptor);
                // Class
                final PluginTestClassTestDescriptor pluginTestClassTestDescriptor = factory.newPluginTestClassTestDescriptor(pluginTestDependencyTestDescriptor, pluginTestInstance);
                if (pluginTestClassTestDescriptor != null) {
                    pluginTestDependencyTestDescriptor.addChild(pluginTestClassTestDescriptor);
                    for (TestDescriptor descriptor : testDescriptor.getChildren()) {
                        if (descriptor instanceof TestMethodTestDescriptor) {
                            final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                            final PluginTestMethodTestDescriptor pluginTestMethodTestDescriptor = factory.newPluginTestMethodTestDescriptor(pluginTestClassTestDescriptor, pluginTestInstance, method);
                            if (pluginTestMethodTestDescriptor != null) {
                                pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
                            }
                        }
                    }
                }
            }
        }
        return pluginTestUnitTestDescriptor;
    }
}
