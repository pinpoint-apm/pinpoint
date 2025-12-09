/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

                final PluginJunitTestMethodTestDescriptor pluginTestMethodTestDescriptor = new PluginJunitTestMethodTestDescriptor(
                        descriptor.getUniqueId(), testClass, testMethod, pluginTestClassTestDescriptor::getEnclosingTestClasses, configuration, testContext
                );
                pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
            }
        }
        return pluginTestClassTestDescriptor;
    }
}
