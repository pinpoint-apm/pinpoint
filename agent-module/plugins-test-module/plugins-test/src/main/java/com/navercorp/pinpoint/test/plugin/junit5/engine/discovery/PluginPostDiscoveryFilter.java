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

package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginForkedTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginJunitTestMethodTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestClassTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestDescriptor;
import com.navercorp.pinpoint.test.plugin.junit5.descriptor.PluginTestMethodTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;

import java.util.Optional;

public class PluginPostDiscoveryFilter implements PostDiscoveryFilter {

    private static final TestDescriptorRegistry registry = new TestDescriptorRegistry();

    @SuppressWarnings("unchecked")
    private static final Class<TestDescriptor>[] registryClass = new Class[] {
        PluginTestDescriptor.class,
        PluginTestClassTestDescriptor.class,
        PluginTestMethodTestDescriptor.class,
        PluginForkedTestClassTestDescriptor.class,
        PluginForkedTestMethodTestDescriptor.class,
        PluginJunitTestClassTestDescriptor.class,
        PluginJunitTestMethodTestDescriptor.class
    };

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        final Optional<String> engineId = testDescriptor.getUniqueId().getEngineId();
        if (engineId.isPresent()) {
            if (engineId.get().equals(PluginTestDescriptor.ENGINE_ID)) {
                if (testDescriptor.isRoot()) {
                    return FilterResult.included("include pinpoint plugin test");
                }
                if (isPluginTestDescriptor(testDescriptor)) {
                    return FilterResult.included("include pinpoint plugin test");
                }
                return FilterResult.excluded("exclude junit test");
            }
        }

        if (testDescriptor.getSource().isPresent()) {
            if (hasPinpointAgent(testDescriptor)) {
                return FilterResult.excluded("exclude pinpoint plugin test");
            }
        }
        return FilterResult.included("includes junit test");
    }

    boolean isPluginTestDescriptor(TestDescriptor testDescriptor) {
        for (Class<TestDescriptor> testDescriptorClass : registryClass) {
            if (testDescriptorClass.isInstance(testDescriptor)) {
                return true;
            }
        }
        return false;
    }

    boolean hasPinpointAgent(TestDescriptor testDescriptor) {
        final Optional<TestSource> source = testDescriptor.getSource();
        if (source.isPresent()) {
            TestSource testSource = source.get();
            if (testSource instanceof ClassSource) {
                Class<?> javaClass = ((ClassSource) testSource).getJavaClass();
                return hasPinpointAgent(javaClass);
            } else if (testSource instanceof MethodSource) {
                Class<?> javaClass = ((MethodSource) testSource).getJavaClass();
                return hasPinpointAgent(javaClass);
            }
        }
        return false;
    }

    boolean hasPinpointAgent(Class<?> javaClass) {
        return registry.getDescriptor(javaClass) != null;
    }
}