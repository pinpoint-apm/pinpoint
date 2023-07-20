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

public class PluginTestUnitTestDescriptor extends PluginTestDescriptor {
    private final Class<?> testClass;

    static String generateDisplayNameForClass(Class<?> testClass) {
        String name = testClass.getName();
        int lastDot = name.lastIndexOf('.');
        return name.substring(lastDot + 1);
    }

    public PluginTestUnitTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration) {
        super(uniqueId, generateDisplayNameForClass(testClass), ClassSource.from(testClass), configuration);
        this.testClass = testClass;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public String getLegacyReportingName() {
        return this.testClass.getName();
    }


    @Override
    public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = createThrowableCollector();

        // @formatter:off
        return context.extend()
                .withThrowableCollector(throwableCollector)
                .build();
        // @formatter:on
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = context.getThrowableCollector();
        throwableCollector.assertEmpty();

        return context;
    }

    @Override
    public void after(JupiterEngineExecutionContext context) {
        ThrowableCollector throwableCollector = context.getThrowableCollector();
        Throwable previousThrowable = throwableCollector.getThrowable();

        // If the previous Throwable was not null when this method was called,
        // that means an exception was already thrown either before or during
        // the execution of this Node. If an exception was already thrown, any
        // later exceptions were added as suppressed exceptions to that original
        // exception unless a more severe exception occurred in the meantime.
        if (previousThrowable != throwableCollector.getThrowable()) {
            throwableCollector.assertEmpty();
        }
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
