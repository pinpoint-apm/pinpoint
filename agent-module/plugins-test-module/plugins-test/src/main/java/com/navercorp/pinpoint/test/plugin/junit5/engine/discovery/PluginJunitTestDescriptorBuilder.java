package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import com.navercorp.pinpoint.profiler.test.junit5.JunitAgentConfigPath;
import com.navercorp.pinpoint.profiler.test.junit5.TestClassWrapper;
import com.navercorp.pinpoint.profiler.test.junit5.TestContext;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestMethodTestDescriptor;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;

import java.lang.reflect.Method;

public class PluginJunitTestDescriptorBuilder implements TestDescriptorBuilder {

    @Override
    public boolean test(Class<?> candidate) {
        return AnnotationUtils.isAnnotated(candidate, JunitAgentConfigPath.class);
    }

    public TestDescriptor build(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
        final TestContext testContext = new TestContext(new TestClassWrapper(((ClassTestDescriptor) testDescriptor).getTestClass()));
        final Class<?> testClass = testContext.createTestClass();

        // Class
        final PluginJunitTestClassTestDescriptor pluginTestClassTestDescriptor = new PluginJunitTestClassTestDescriptor(testDescriptor.getUniqueId(), testClass, configuration, testContext);
        //switchTestDescriptor(testDescriptor, pluginTestClassTestDescriptor);
        for (TestDescriptor descriptor : testDescriptor.getChildren()) {
            if (descriptor instanceof TestMethodTestDescriptor) {
                final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                final Method testMethod = ReflectionUtils.findMethod(testClass, method.getName(), method.getParameterTypes()).orElseThrow(() -> new IllegalStateException("not found method"));

                final PluginJunitTestMethodTestDescriptor pluginTestMethodTestDescriptor = new PluginJunitTestMethodTestDescriptor(descriptor.getUniqueId(), testClass, testMethod, configuration, testContext);
                pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
            }
        }
        return pluginTestClassTestDescriptor;
    }
}
