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

package com.navercorp.pinpoint.test.plugin.junit5.descriptor;

import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import static org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory.createThrowableCollector;

public class PluginTestDependencyTestDescriptor extends PluginTestDescriptor {

    public PluginTestDependencyTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration, String displayName) {
        super(uniqueId, displayName, ClassSource.from(testClass), configuration);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
        ThrowableCollector throwableCollector = createThrowableCollector();

        // @formatter:off
        return context.extend()
                .withThrowableCollector(throwableCollector)
                .build();
        // @formatter:on
    }
}