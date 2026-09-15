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

package com.navercorp.pinpoint.test.plugin.junit5.engine;

import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.TestDescriptorBuilder;
import com.navercorp.pinpoint.test.plugin.junit5.engine.discovery.TestDescriptorRegistry;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolverFactory;
import org.eclipse.aether.ConfigurationProperties;
import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.TestClassAware;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.LauncherStoreFacade;
import org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PluginTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TestDescriptorRegistry registry = new TestDescriptorRegistry();
    // created when the first plugin test suite asks for it, so that loading the engine or discovering junit-only tests does not wire a maven repository system
    private DependencyResolverFactory resolverFactory;

    public PluginTestEngine() {
        logger.debug(() -> "PluginTestEngine created");
    }

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
        DefaultJupiterConfiguration jupiterConfiguration = new DefaultJupiterConfiguration(discoveryRequest.getConfigurationParameters(), discoveryRequest.getOutputDirectoryProvider());
        JupiterConfiguration configuration = new CachingJupiterConfiguration(jupiterConfiguration);
        JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(uniqueId, configuration);
        new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);

        // Plugin IT
        List<TestDescriptor> removedTestDescriptorList = new ArrayList<>();
        List<TestDescriptor> pluginTestDescriptorList = new ArrayList<>();

        try {
            // only the raw descriptors just produced by the jupiter resolver reach this loop;
            // the plugin wrappers replacing them are added after it
            for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
                if (testDescriptor instanceof TestClassAware) {
                    final Class<?> testClass = ((TestClassAware) testDescriptor).getTestClass();
                    TestDescriptor pluginTestDescriptor = getDescriptor(testDescriptor, testClass, configuration);
                    if (pluginTestDescriptor != null) {
                        pluginTestDescriptorList.add(pluginTestDescriptor);
                        removedTestDescriptorList.add(testDescriptor);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e, () -> "Failed to discover " + e.getMessage());
        }

        for (TestDescriptor removedTestDescriptor : removedTestDescriptorList) {
            removedTestDescriptor.removeFromHierarchy();
        }

        for (TestDescriptor addTestDescriptor : pluginTestDescriptorList) {
            engineDescriptor.addChild(addTestDescriptor);
        }

        return engineDescriptor;
    }

    private TestDescriptor getDescriptor(TestDescriptor testDescriptor, Class<?> testClass, JupiterConfiguration configuration) {
        TestDescriptorBuilder builder = registry.getDescriptor(testClass);
        if (builder == null) {
            return null;
        }
        return builder.build(testDescriptor, testClass, configuration, this::getResolverFactory);
    }

    private synchronized DependencyResolverFactory getResolverFactory() {
        if (this.resolverFactory == null) {
            this.resolverFactory = new DependencyResolverFactory(resolverOption());
        }
        return this.resolverFactory;
    }

    private static Map<String, Object> resolverOption() {
        Map<String, Object> resolverOption = new HashMap<>();
        resolverOption.put(ConfigurationProperties.CONNECT_TIMEOUT, TimeUnit.SECONDS.toMillis(5));
        resolverOption.put(ConfigurationProperties.REQUEST_TIMEOUT, TimeUnit.MINUTES.toMillis(5));
        return resolverOption;
    }


    @Override
    public JupiterEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        return new JupiterEngineExecutionContext(request.getEngineExecutionListener(),
                getJupiterConfiguration(request), new LauncherStoreFacade(request.getStore()));
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
