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

import com.navercorp.pinpoint.test.plugin.junit5.engine.support.PluginTestReport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import static org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory.createThrowableCollector;

public class PluginForkedTestClassTestDescriptor extends ClassTestDescriptor {

    private PluginTestReport testReport;

    public PluginForkedTestClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration) {
        super(uniqueId, testClass, configuration);
    }

    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = createThrowableCollector();

        final ExtensionContext.Store store = context.getExtensionContext().getStore(PluginForkedTestUnitTestDescriptor.NAMESPACE);
        this.testReport = store.get(getUniqueId().toString(), PluginTestReport.class);

        return context.extend()
                .withThrowableCollector(throwableCollector)
                .build();
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = context.getThrowableCollector();
        if (testReport != null && testReport.isStarted()) {
            if (testReport.getOutput() != null) {
                for (String line : testReport.getOutput()) {
                    System.out.println(line);
                }
            }
            if (testReport.getResult().getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
                throwableCollector.execute(() -> {
                    Throwable throwable = testReport.getResult().getThrowable().orElse(new IllegalStateException("unknown"));
                    throwable.printStackTrace();
                    throw throwable;
                });
            }
        }

        throwableCollector.assertEmpty();

        return context;
    }

    @Override
    public void after(JupiterEngineExecutionContext context) {
    }
}
