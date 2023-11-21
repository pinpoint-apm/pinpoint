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

import com.navercorp.pinpoint.profiler.test.junit5.TestClassWrapper;
import com.navercorp.pinpoint.profiler.test.junit5.TestContext;
import com.navercorp.pinpoint.test.plugin.DefaultPluginForkedTestSuite;
import com.navercorp.pinpoint.test.plugin.DefaultPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PluginForkedTestInstance;
import com.navercorp.pinpoint.test.plugin.PluginTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestUnitTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDependencyTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestUnitTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithJunitAgent;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithPinpointAgent;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithPluginForkedTest;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.predicates.IsTestClassWithPluginTest;
import com.navercorp.pinpoint.test.plugin.shared.PluginSharedInstance;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {
    private static final IsTestClassWithPinpointAgent isTestClassWithPinpointAgent = new IsTestClassWithPinpointAgent();
    private static final IsTestClassWithPluginTest isTestClassWithPluginTest = new IsTestClassWithPluginTest();
    private static final IsTestClassWithPluginForkedTest isTestClassWithPluginForkedTest = new IsTestClassWithPluginForkedTest();
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

        // Plugin IT
        List<TestDescriptor> removedTestDescriptorList = new ArrayList<>();
        List<TestDescriptor> pluginTestDescriptorList = new ArrayList<>();

        try {
            for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
                if (testDescriptor instanceof ClassTestDescriptor) {
                    final Class<?> testClass = ((ClassTestDescriptor) testDescriptor).getTestClass();
                    TestDescriptor pluginTestDescriptor = null;
                    if (isTestClassWithPluginTest.test(testClass)) {
                        pluginTestDescriptor = addPluginTestDescriptor(testDescriptor, configuration);
                    } else if (isTestClassWithPluginForkedTest.test(testClass)) {
                        pluginTestDescriptor = addPluginForkedTestDescriptor(testDescriptor, configuration);
                    } else if (isTestClassWithJunitAgent.test(testClass)) {
                        pluginTestDescriptor = addPluginJunitTestDescriptor(testDescriptor, configuration);
                    }

                    if (pluginTestDescriptor != null) {
                        pluginTestDescriptorList.add(pluginTestDescriptor);
                        removedTestDescriptorList.add(testDescriptor);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to discover");
            e.printStackTrace();
        }

        for (TestDescriptor removedTestDescriptor : removedTestDescriptorList) {
            removedTestDescriptor.removeFromHierarchy();
        }

        for (TestDescriptor addTestDescriptor : pluginTestDescriptorList) {
            engineDescriptor.addChild(addTestDescriptor);
        }

        return engineDescriptor;
    }

    TestDescriptor addPluginTestDescriptor(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
        final DefaultPluginTestSuite testSuite = new DefaultPluginTestSuite(((ClassTestDescriptor) testDescriptor).getTestClass());
        final PluginSharedInstance sharedInstance = testSuite.getPluginSharedInstance();
        final List<PluginTestInstance> testInstanceList = testSuite.getPluginTestInstanceList();

        // Unit
        final PluginTestUnitTestDescriptor pluginTestUnitTestDescriptor = new PluginTestUnitTestDescriptor(testDescriptor.getUniqueId(), ((ClassTestDescriptor) testDescriptor).getTestClass(), configuration, sharedInstance);
        for (PluginTestInstance pluginTestInstance : testInstanceList) {
            final String testId = pluginTestInstance.getTestId();
            // Dependency
            final PluginTestDependencyTestDescriptor pluginTestDependencyTestDescriptor = toPluginTestDependencyTestDescriptor(configuration, pluginTestUnitTestDescriptor, testId);
            if (pluginTestDependencyTestDescriptor != null) {
                pluginTestUnitTestDescriptor.addChild(pluginTestDependencyTestDescriptor);
                // Class
                final PluginTestClassTestDescriptor pluginTestClassTestDescriptor = toPluginTestClassTestDescriptor(configuration, pluginTestDependencyTestDescriptor, pluginTestInstance);
                if (pluginTestClassTestDescriptor != null) {
                    pluginTestDependencyTestDescriptor.addChild(pluginTestClassTestDescriptor);
                    for (TestDescriptor descriptor : testDescriptor.getChildren()) {
                        if (descriptor instanceof TestMethodTestDescriptor) {
                            final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                            final PluginTestMethodTestDescriptor pluginTestMethodTestDescriptor = toPluginTestMethodTestDescriptor(configuration, pluginTestClassTestDescriptor, pluginTestInstance, method);
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

    TestDescriptor addPluginForkedTestDescriptor(TestDescriptor testDescriptor, JupiterConfiguration configuration) {
        final DefaultPluginForkedTestSuite testSuite = new DefaultPluginForkedTestSuite(((ClassTestDescriptor) testDescriptor).getTestClass());
        final List<PluginForkedTestInstance> testInstanceList = testSuite.getPluginTestInstanceList();

        // Unit
        final PluginForkedTestUnitTestDescriptor pluginTestUnitTestDescriptor = new PluginForkedTestUnitTestDescriptor(testDescriptor.getUniqueId(), ((ClassTestDescriptor) testDescriptor).getTestClass(), configuration, testInstanceList);
        for (PluginForkedTestInstance pluginTestInstance : testInstanceList) {
            final String testId = pluginTestInstance.getTestId();
            // Dependency
            final PluginForkedTestDependencyTestDescriptor pluginTestDependencyTestDescriptor = toPluginForkedTestDependencyTestDescriptor(configuration, pluginTestUnitTestDescriptor, testId);
            if (pluginTestDependencyTestDescriptor != null) {
                pluginTestUnitTestDescriptor.addChild(pluginTestDependencyTestDescriptor);
                // Class
                final PluginForkedTestClassTestDescriptor pluginTestClassTestDescriptor = toPluginForkedTestClassTestDescriptor(configuration, pluginTestDependencyTestDescriptor);
                if (pluginTestClassTestDescriptor != null) {
                    pluginTestDependencyTestDescriptor.addChild(pluginTestClassTestDescriptor);
                    for (TestDescriptor descriptor : testDescriptor.getChildren()) {
                        if (descriptor instanceof TestMethodTestDescriptor) {
                            final Method method = ((TestMethodTestDescriptor) descriptor).getTestMethod();
                            final PluginForkedTestMethodTestDescriptor pluginTestMethodTestDescriptor = toPluginForkedTestMethodTestDescriptor(configuration, pluginTestClassTestDescriptor, method);
                            pluginTestClassTestDescriptor.addChild(pluginTestMethodTestDescriptor);
                        }
                    }
                }
            }
        }
        return pluginTestUnitTestDescriptor;
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

    private static PluginTestDependencyTestDescriptor toPluginTestDependencyTestDescriptor(JupiterConfiguration configuration, PluginTestUnitTestDescriptor parentTestDescriptor, String testId) {
        try {
            return new PluginTestDependencyTestDescriptor(parentTestDescriptor.getUniqueId().append("dependency", testId), parentTestDescriptor.getTestClass(), configuration, testId);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return null;
        }
    }

    private static PluginForkedTestDependencyTestDescriptor toPluginForkedTestDependencyTestDescriptor(JupiterConfiguration configuration, PluginForkedTestUnitTestDescriptor parentTestDescriptor, String testId) {
        try {
            return new PluginForkedTestDependencyTestDescriptor(parentTestDescriptor.getUniqueId().append("dependency", testId), parentTestDescriptor.getTestClass(), configuration, testId);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return null;
        }
    }

    private static PluginTestClassTestDescriptor toPluginTestClassTestDescriptor(JupiterConfiguration configuration, PluginTestDependencyTestDescriptor parentTestDescriptor, PluginTestInstance pluginTestInstance) {
        try {
            final Class<?> testClass = pluginTestInstance.getTestClass();
            return new PluginTestClassTestDescriptor(parentTestDescriptor.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass, configuration, pluginTestInstance);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return null;
        }
    }

    private static PluginForkedTestClassTestDescriptor toPluginForkedTestClassTestDescriptor(JupiterConfiguration configuration, PluginForkedTestDependencyTestDescriptor parentTestDescriptor) {
        try {
            final Class<?> testClass = parentTestDescriptor.getTestClass();
            return new PluginForkedTestClassTestDescriptor(parentTestDescriptor.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass, configuration);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return null;
        }
    }

    private static PluginTestMethodTestDescriptor toPluginTestMethodTestDescriptor(JupiterConfiguration configuration, PluginTestClassTestDescriptor parentTestDescriptor, PluginTestInstance pluginTestInstance, Method method) {
        try {
            final Class<?> testClass = pluginTestInstance.getTestClass();
            final Method testMethod = ReflectionUtils.findMethod(testClass, method.getName(), method.getParameterTypes()).orElseThrow(() -> new IllegalStateException("not found method"));
            return new PluginTestMethodTestDescriptor(parentTestDescriptor.getUniqueId().append(TestMethodTestDescriptor.SEGMENT_TYPE, testMethod.getName()), testClass, testMethod, configuration, pluginTestInstance);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return null;
        }
    }

    private static PluginForkedTestMethodTestDescriptor toPluginForkedTestMethodTestDescriptor(JupiterConfiguration configuration, PluginForkedTestClassTestDescriptor parentTestDescriptor, Method method) {
        try {
            final Class<?> testClass = parentTestDescriptor.getTestClass();
            final Method testMethod = ReflectionUtils.findMethod(testClass, method.getName(), method.getParameterTypes()).orElseThrow(() -> new IllegalStateException("not found method"));
            String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
            return new PluginForkedTestMethodTestDescriptor(parentTestDescriptor.getUniqueId().append(TestMethodTestDescriptor.SEGMENT_TYPE, methodId), testClass, testMethod, configuration);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return null;
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
