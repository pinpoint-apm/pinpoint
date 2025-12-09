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

package com.navercorp.pinpoint.test.plugin.junit5.descriptor;

import com.navercorp.pinpoint.test.plugin.PluginTestInstance;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class PluginTestMethodTestDescriptor extends TestMethodTestDescriptor {

    private final PluginTestInstance pluginTestInstance;
    private final MethodSource source;

    public PluginTestMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod, Supplier<List<Class<?>>> enclosingInstanceTypes, JupiterConfiguration configuration, PluginTestInstance pluginTestInstance) {
        super(uniqueId, testClass, testMethod, enclosingInstanceTypes, configuration);
        this.pluginTestInstance = pluginTestInstance;
        this.source = MethodSource.from(testClass.getName(), testMethod.getName() + "[" + pluginTestInstance.getTestId() + "]");
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
        return this.pluginTestInstance.call(() -> {
            return super.prepare(context);
        }, false);
    }

    @Override
    public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        return this.pluginTestInstance.call(() -> {
            return super.execute(context, dynamicTestExecutor);
        }, true);
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(source);
    }
}