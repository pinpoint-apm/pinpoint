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

import com.navercorp.pinpoint.test.plugin.PluginForkedTestInstance;
import com.navercorp.pinpoint.test.plugin.junit5.engine.support.PluginForkedTestThrowableCollector;
import com.navercorp.pinpoint.test.plugin.junit5.engine.support.PluginTestReport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class PluginForkedTestMethodTestDescriptor extends TestMethodTestDescriptor {

    private final PluginForkedTestInstance pluginTestInstance;
    private final MethodSource source;
    private PluginTestReport testReport;


    public PluginForkedTestMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod, Supplier<List<Class<?>>> enclosingInstanceTypes, JupiterConfiguration configuration, PluginForkedTestInstance pluginTestInstance) {
        super(uniqueId, testClass, testMethod, enclosingInstanceTypes, configuration);
        this.pluginTestInstance = pluginTestInstance;
        this.source = MethodSource.from(testClass.getName(), testMethod.getName() + "[" + pluginTestInstance.getTestId() + "]");
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = new PluginForkedTestThrowableCollector();

        final ExtensionContext.Store store = context.getExtensionContext().getStore(PluginForkedTestUnitTestDescriptor.NAMESPACE);
        this.testReport = store.get(getUniqueId().toString(), PluginTestReport.class);

        return context.extend()
                .withThrowableCollector(throwableCollector)
                .build();
    }

    @Override
    public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        PluginForkedTestThrowableCollector throwableCollector = (PluginForkedTestThrowableCollector) context.getThrowableCollector();
        PluginTestReport testReport = this.testReport;
        if (testReport != null && testReport.isStarted()) {
            if (testReport.getOutput() != null) {
                for (String line : testReport.getOutput()) {
                    System.out.println(line);
                }
            }

            final TestExecutionResult result = testReport.getResult();
            if (result.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
                throwableCollector.execute(() -> {
                    Throwable throwable = result.getThrowable().orElse(new IllegalStateException("unknown"));
                    throwable.printStackTrace();
                    throw throwable;
                });
            }
        }

        return context;
    }

    @Override
    public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) {
        PluginTestReport testReport = this.testReport;
        if (testReport != null) {
            if (testReport.isSkipped()) {
                if (testReport.getSkipReason() != null) {
                    return SkipResult.skip(testReport.getSkipReason());
                }
                return SkipResult.skip("");
            }
        } else {
            return SkipResult.skip("");
        }
        return SkipResult.doNotSkip();
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(source);
    }
}