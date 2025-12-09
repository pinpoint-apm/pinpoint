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
import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
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
import java.util.List;
import java.util.Optional;

public class PluginTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TestDescriptorRegistry registry = new TestDescriptorRegistry();

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
            for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
                if (testDescriptor instanceof ClassTestDescriptor) {
                    final Class<?> testClass = ((ClassTestDescriptor) testDescriptor).getTestClass();
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
        return builder.build(testDescriptor, configuration);
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
