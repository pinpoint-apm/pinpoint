/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.junit5.engine;

import com.navercorp.pinpoint.test.junit5.TestClassWrapper;
import com.navercorp.pinpoint.test.junit5.TestContext;
import com.navercorp.pinpoint.test.plugin.DefaultPluginForkedTestSuite;
import com.navercorp.pinpoint.test.plugin.PluginForkedTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestUnitTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithJunitAgent;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithPinpointAgent;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.tinylog.TaggedLogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginForkedTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {
    private static final TaggedLogger logger = TestLogger.getLogger();

    private static final IsTestClassWithPinpointAgent isTestClassWithPinpointAgent = new IsTestClassWithPinpointAgent();

    private static final IsTestClassWithJunitAgent isTestClassWithJunitAgent = new IsTestClassWithJunitAgent();

    @Override
    public String getId() {
        return PluginTestDescriptor.ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("com.navercorp.pinpoint");
    }

    /**
     * Returns {@code junit-jupiter-engine} as the artifact ID.
     */
    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("pinpoint-plugin-test-engine");
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        JupiterConfiguration configuration = new CachingJupiterConfiguration(new DefaultJupiterConfiguration(discoveryRequest.getConfigurationParameters()));
        JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(uniqueId, configuration);
        new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);

        List<TestDescriptor> removedTestDescriptorList = new ArrayList<>();
        List<TestDescriptor> pluginForkedTestDescriptorList = new ArrayList<>();
        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            if (testDescriptor instanceof ClassTestDescriptor) {
                final Class<?> testClass = ((ClassTestDescriptor) testDescriptor).getTestClass();
                if (isTestClassWithPinpointAgent.test(testClass)) {
                    TestDescriptor pluginTestDescriptor = addPluginForkedTestDescriptor(testDescriptor, configuration);
                    pluginForkedTestDescriptorList.add(pluginTestDescriptor);
                    removedTestDescriptorList.add(testDescriptor);
                } else if (isTestClassWithJunitAgent.test(testClass)) {
                    TestDescriptor pluginTestDescriptor = addPluginJunitTestDescriptor(testDescriptor, configuration);
                    pluginForkedTestDescriptorList.add(pluginTestDescriptor);
                    removedTestDescriptorList.add(testDescriptor);
                }
            }
        }

        for (TestDescriptor testDescriptor : removedTestDescriptorList) {
            testDescriptor.removeFromHierarchy();
        }

        for (TestDescriptor testDescriptor : pluginForkedTestDescriptorList) {
            engineDescriptor.addChild(testDescriptor);
        }

        return engineDescriptor;
    }

    TestDescriptor addPluginForkedTestDescriptor(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
        final DefaultPluginForkedTestSuite testSuite = new DefaultPluginForkedTestSuite(((ClassTestDescriptor) testDescriptor).getTestClass());
        final List<PluginForkedTestInstance> testInstanceList = testSuite.getPluginTestInstanceList();

        // Unit
        final PluginForkedTestUnitTestDescriptor pluginTestUnitTestDescriptor = new PluginForkedTestUnitTestDescriptor(testDescriptor.getUniqueId(), ((ClassTestDescriptor) testDescriptor).getTestClass(), configuration, testInstanceList);
        // switchTestDescriptor(testDescriptor, pluginTestUnitTestDescriptor);

        for (PluginForkedTestInstance pluginTestInstance : testInstanceList) {
            final String testId = pluginTestInstance.getTestId();
            // Dependency
            final PluginForkedTestDependencyTestDescriptor pluginTestDependencyTestDescriptor = toPluginTestDependencyTestDescriptor(configuration, pluginTestUnitTestDescriptor, testId);
            pluginTestUnitTestDescriptor.addChild(pluginTestDependencyTestDescriptor);

            // Class
            final PluginForkedTestClassTestDescriptor pluginTestClassTestDescriptor = toPluginTestClassTestDescriptor(configuration, pluginTestDependencyTestDescriptor);
            pluginTestDependencyTestDescriptor.addChild(pluginTestClassTestDescriptor);
            for (TestDescriptor descriptor : testDescriptor.getChildren()) {
                if (descriptor instanceof TestMethodTestDescriptor) {
                    final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                    final PluginForkedTestMethodTestDescriptor pluginTestMethodTestDescriptor = toPluginTestMethodTestDescriptor(configuration, pluginTestClassTestDescriptor, method);
                    pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
                }
            }
        }
        return pluginTestUnitTestDescriptor;
    }

    private static PluginForkedTestDependencyTestDescriptor toPluginTestDependencyTestDescriptor(JupiterConfiguration configuration, PluginForkedTestUnitTestDescriptor parentTestDescriptor, String testId) {
        return new PluginForkedTestDependencyTestDescriptor(parentTestDescriptor.getUniqueId().append("dependency", testId), parentTestDescriptor.getTestClass(), configuration, testId);
    }

    private static PluginForkedTestClassTestDescriptor toPluginTestClassTestDescriptor(JupiterConfiguration configuration, PluginForkedTestDependencyTestDescriptor parentTestDescriptor) {
        final Class<?> testClass = parentTestDescriptor.getTestClass();
        return new PluginForkedTestClassTestDescriptor(parentTestDescriptor.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass, configuration);
    }

    private static PluginForkedTestMethodTestDescriptor toPluginTestMethodTestDescriptor(JupiterConfiguration configuration, PluginForkedTestClassTestDescriptor parentTestDescriptor, Method method) {
        final Class<?> testClass = parentTestDescriptor.getTestClass();
        final Method testMethod = method;
        String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
        return new PluginForkedTestMethodTestDescriptor(parentTestDescriptor.getUniqueId().append(TestMethodTestDescriptor.SEGMENT_TYPE, methodId), testClass, testMethod, configuration);
    }

    TestDescriptor addPluginJunitTestDescriptor(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
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

    private void switchTestDescriptor(TestDescriptor oldTestDescriptor, TestDescriptor newTestDescriptor) {
        if (Boolean.FALSE == oldTestDescriptor.isRoot()) {
            TestDescriptor rootTestDescriptor = oldTestDescriptor.getParent().orElseThrow(() -> new IllegalArgumentException("not found root"));
            rootTestDescriptor.removeChild(oldTestDescriptor);
            rootTestDescriptor.addChild(newTestDescriptor);
        }
    }

    @Override
    public JupiterEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        return new JupiterEngineExecutionContext(request.getEngineExecutionListener(),
                getJupiterConfiguration(request));
    }

    private JupiterConfiguration getJupiterConfiguration(ExecutionRequest request) {
        JupiterEngineDescriptor engineDescriptor = (JupiterEngineDescriptor) request.getRootTestDescriptor();
        return engineDescriptor.getConfiguration();
    }

    @Override
    public ThrowableCollector.Factory createThrowableCollectorFactory(ExecutionRequest request) {
        return JupiterThrowableCollectorFactory::createThrowableCollector;
    }
}
