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

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.test.junit5.IsRootSpan;
import com.navercorp.pinpoint.test.junit5.TestContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.UniqueId;

import java.lang.reflect.Method;

public class PluginJunitTestMethodTestDescriptor extends TestMethodTestDescriptor {

    private TestContext testContext;
    private Method testMethod;

    public PluginJunitTestMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod, JupiterConfiguration configuration, TestContext testContext) {
        super(uniqueId, testClass, testMethod, configuration);
        this.testContext = testContext;
        this.testMethod = testMethod;
    }

    @Override
    public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        ExtensionContext extensionContext = context.getExtensionContext();
        setupBaseTest(extensionContext.getRequiredTestInstance());

        beginTracing();
        final Thread thread = Thread.currentThread();
        final ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(testContext.getClassLoader());
            super.execute(context, dynamicTestExecutor);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
            endTracing();
        }

        return context;
    }

    private void setupBaseTest(Object test) {
        // It's safe to cast
        final Class<?> baseTestClass = testContext.getBaseTestClass();
        if (baseTestClass.isInstance(test)) {
            try {
                Method reset = baseTestClass.getDeclaredMethod("setup", com.navercorp.pinpoint.test.junit5.TestContext.class);
                reset.invoke(test, testContext);
            } catch (Exception e) {
                throw new RuntimeException("setCurrentHolder Error. Caused by:" + e.getMessage(), e);
            }
        }
    }


    private void beginTracing() {
        if (shouldCreateNewTraceObject()) {
            TraceContext traceContext = getTraceContext();
            Trace trace = traceContext.newTraceObject();
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(ServiceType.TEST);
        }
    }

    private void endTracing() {
        if (shouldCreateNewTraceObject()) {
            TraceContext traceContext = getTraceContext();
            try {
                Trace trace = traceContext.currentRawTraceObject();
                if (trace == null) {
                    // Trace is already detached from the ThreadLocal storage.
                    // Happens when root trace method is tested without @IsRootSpan.
                    String traceObjectAlreadyDetachedMessage = "Trace object already detached. If you're testing a trace root, please add @IsRootSpan to the test method";
                    throw new IllegalStateException(traceObjectAlreadyDetachedMessage);
                } else {
                    trace.close();
                }
            } finally {
                traceContext.removeTraceObject();
            }
        }
    }

    private boolean shouldCreateNewTraceObject() {
        IsRootSpan isRootSpan = this.testMethod.getAnnotation(IsRootSpan.class);
        return isRootSpan == null || !isRootSpan.value();
    }

    private TraceContext getTraceContext() {
        DefaultApplicationContext mockApplicationContext = testContext.getDefaultApplicationContext();
        return mockApplicationContext.getTraceContext();
    }

}
