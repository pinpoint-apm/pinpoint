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

import com.navercorp.pinpoint.test.plugin.DefaultPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PluginTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestUnitTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithPinpointAgent;
import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class PluginTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {
    private static final IsTestClassWithPinpointAgent isTestClassWithPinpointAgent = new IsTestClassWithPinpointAgent();

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

        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            if (testDescriptor instanceof ClassTestDescriptor) {
                final Class<?> testClass = ((ClassTestDescriptor) testDescriptor).getTestClass();
                if (Boolean.FALSE == isTestClassWithPinpointAgent.test(testClass)) {
                    // Skip non pinpoint plugin testcase
                    continue;
                }

                final DefaultPluginTestSuite testSuite = new DefaultPluginTestSuite(((ClassTestDescriptor) testDescriptor).getTestClass());
                final List<PluginTestInstance> testInstanceList = testSuite.getPluginTestInstanceList();

                // Unit
                final PluginTestUnitTestDescriptor pluginTestUnitTestDescriptor = new PluginTestUnitTestDescriptor(testDescriptor.getUniqueId(), ((ClassTestDescriptor) testDescriptor).getTestClass(), configuration);
                switchTestDescriptor(testDescriptor, pluginTestUnitTestDescriptor);

                for (PluginTestInstance pluginTestInstance : testInstanceList) {
                    final String testId = pluginTestInstance.getTestId();
                    // Dependency
                    final PluginTestDependencyTestDescriptor pluginTestDependencyTestDescriptor = toPluginTestDependencyTestDescriptor(configuration, pluginTestUnitTestDescriptor, testId);
                    pluginTestUnitTestDescriptor.addChild(pluginTestDependencyTestDescriptor);
                    // Class
                    final PluginTestClassTestDescriptor pluginTestClassTestDescriptor = toPluginTestClassTestDescriptor(configuration, pluginTestDependencyTestDescriptor, pluginTestInstance);
                    pluginTestDependencyTestDescriptor.addChild(pluginTestClassTestDescriptor);
                    for (TestDescriptor descriptor : testDescriptor.getChildren()) {
                        if (descriptor instanceof TestMethodTestDescriptor) {
                            final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                            final PluginTestMethodTestDescriptor pluginTestMethodTestDescriptor = toPluginTestMethodTestDescriptor(configuration, pluginTestClassTestDescriptor, pluginTestInstance, method);
                            pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
                        }
                    }
                }
            }
        }

        return engineDescriptor;
    }

    private static PluginTestDependencyTestDescriptor toPluginTestDependencyTestDescriptor(JupiterConfiguration configuration, PluginTestUnitTestDescriptor parentTestDescriptor, String testId) {
        return new PluginTestDependencyTestDescriptor(parentTestDescriptor.getUniqueId().append("dependency", testId), parentTestDescriptor.getTestClass(), configuration, testId);
    }

    private static PluginTestClassTestDescriptor toPluginTestClassTestDescriptor(JupiterConfiguration configuration, PluginTestDependencyTestDescriptor parentTestDescriptor, PluginTestInstance pluginTestInstance) {
        final Class<?> testClass = pluginTestInstance.getTestClass();
        return new PluginTestClassTestDescriptor(parentTestDescriptor.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass, configuration, pluginTestInstance);
    }

    private static PluginTestMethodTestDescriptor toPluginTestMethodTestDescriptor(JupiterConfiguration configuration, PluginTestClassTestDescriptor parentTestDescriptor, PluginTestInstance pluginTestInstance, Method method) {
        final Class<?> testClass = pluginTestInstance.getTestClass();
        final Method testMethod = ReflectionUtils.findMethod(testClass, method.getName(), method.getParameterTypes()).orElseThrow(() -> new IllegalStateException("not found method"));
        return new PluginTestMethodTestDescriptor(parentTestDescriptor.getUniqueId().append(TestMethodTestDescriptor.SEGMENT_TYPE, testMethod.getName()), testClass, testMethod, configuration, pluginTestInstance);
    }

    private void switchTestDescriptor(TestDescriptor oldTestDescriptor, TestDescriptor newTestDescriptor) {
        if (Boolean.FALSE == oldTestDescriptor.isRoot()) {
            TestDescriptor rootTestDescriptor = oldTestDescriptor.getParent().orElseThrow(() -> new IllegalArgumentException("not found root"));
            rootTestDescriptor.removeChild(oldTestDescriptor);
            rootTestDescriptor.addChild(newTestDescriptor);
        }
    }

//    @Override
//    public void execute(ExecutionRequest request) {
//        System.out.println("#### execute");
//        TestDescriptor rootTestDescriptor = request.getRootTestDescriptor();
//
//        EngineExecutionListener listener = request.getEngineExecutionListener();
//
//        for (TestDescriptor classTestDescriptor : rootTestDescriptor.getChildren()) {
//            listener.executionStarted(classTestDescriptor);
//            for (TestDescriptor pluginTestDescriptor : classTestDescriptor.getChildren()) {
//                listener.executionStarted(pluginTestDescriptor);
//                for (TestDescriptor methodTestDescriptor : pluginTestDescriptor.getChildren()) {
//                    listener.executionStarted(methodTestDescriptor);
//                    listener.executionFinished(methodTestDescriptor, TestExecutionResult.successful());
//                }
//                listener.executionFinished(pluginTestDescriptor, TestExecutionResult.successful());
//            }
//            listener.executionFinished(classTestDescriptor, TestExecutionResult.successful());
//        }
//        PluginTestDescriptor container = new PluginTestDescriptor(rootTestDescriptor.getUniqueId().append("container", "1"), "container #1");
//        rootTestDescriptor.addChild(container);
//
//        listener.dynamicTestRegistered(container);
//        listener.executionStarted(container);
//
//        UniqueId containerUid = container.getUniqueId();
//
//        for (TestDescriptor testDescriptor : rootTestDescriptor.getChildren()) {
//            System.out.println("uniqueId=" + testDescriptor.getUniqueId());
//            if (testDescriptor instanceof ClassBasedTestDescriptor) {
//                Class<?> clazz = ((ClassBasedTestDescriptor) testDescriptor).getTestClass();
//                PluginTestSuite testSuite = new PluginTestSuite(clazz);
//
//                List<PinpointPluginTestInstance> testInstanceList = testSuite.getChildren();
//                System.out.println("## testInstanceList " + testInstanceList);
//                for (PinpointPluginTestInstance instance : testInstanceList) {
//                    try {
//                        final String testId = instance.getTestId();
//                        PluginTestDescriptor pluginTestDescriptor = new PluginTestDescriptor(testDescriptor.getUniqueId().append("lib", testId), testId);
//                        testDescriptor.addChild(pluginTestDescriptor);
//                        listener.dynamicTestRegistered(pluginTestDescriptor);
//
//                        listener.executionStarted(pluginTestDescriptor);
//                        startTest(instance);
//                        listener.executionFinished(pluginTestDescriptor, TestExecutionResult.successful());
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//                }
//                listener.executionFinished(testDescriptor, TestExecutionResult.successful());
//    }
//    }
//
//        listener.executionFinished(container, TestExecutionResult.successful());
//
//
//    }

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
