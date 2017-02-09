/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.junit4;

import java.lang.reflect.Method;


import com.navercorp.pinpoint.test.MockApplicationContext;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author hyungil.jeong
 * @author emeroad
 */
public final class PinpointJUnit4ClassRunner extends BlockJUnit4ClassRunner {

    private static final Logger logger = LoggerFactory.getLogger(PinpointJUnit4ClassRunner.class);

    private static TestContext testContext;

    public PinpointJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("PinpointJUnit4ClassRunner constructor called with [{}].", clazz);
        }
    }

    private void beforeTestClass() {
        try {
            // TODO fix static TestContext
            if (testContext == null) {
                logger.debug("traceContext is null");
                testContext = new TestContext();
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    protected TestClass createTestClass(Class<?> testClass) {
        logger.debug("createTestClass {}", testClass);
        beforeTestClass();
        return testContext.createTestClass(testClass);
    }

    private TraceContext getTraceContext() {
        MockApplicationContext mockApplicationContext = testContext.getMockApplicationContext();
        return mockApplicationContext.getTraceContext();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        beginTracing(method);
        final Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(testContext.getClassLoader());
            super.runChild(method, notifier);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
            endTracing(method, notifier);
        }
    }

    private void beginTracing(FrameworkMethod method) {
        if (shouldCreateNewTraceObject(method)) {
            TraceContext traceContext = getTraceContext();
            Trace trace = traceContext.newTraceObject();
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(ServiceType.TEST);
        }
    }

    private void endTracing(FrameworkMethod method, RunNotifier notifier) {
        if (shouldCreateNewTraceObject(method)) {
            TraceContext traceContext = getTraceContext();
            try {
                Trace trace = traceContext.currentRawTraceObject();
                if (trace == null) {
                    // Trace is already detached from the ThreadLocal storage.
                    // Happens when root trace method is tested without @IsRootSpan.
                    EachTestNotifier testMethodNotifier = new EachTestNotifier(notifier, super.describeChild(method));
                    String traceObjectAlreadyDetachedMessage = "Trace object already detached. If you're testing a trace root, please add @IsRootSpan to the test method";
                    testMethodNotifier.addFailure(new IllegalStateException(traceObjectAlreadyDetachedMessage));
                } else {
                    trace.close();
                }
            } finally {
                traceContext.removeTraceObject();
            }
        }
    }

    private boolean shouldCreateNewTraceObject(FrameworkMethod method) {
        IsRootSpan isRootSpan = method.getAnnotation(IsRootSpan.class);
        return isRootSpan == null || !isRootSpan.value();
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return super.methodInvoker(method, test);
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, final Object target, Statement statement) {
        Statement before =  super.withBefores(method, target, statement);
        BeforeCallbackStatement callbackStatement = new BeforeCallbackStatement(before, new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setupBaseTest(target);
            }
        });
        return callbackStatement;
    }

    private void setupBaseTest(Object test) {
        logger.debug("setupBaseTest");
        // It's safe to cast
        final Class<?> baseTestClass = testContext.getBaseTestClass();
        if (baseTestClass.isInstance(test)) {
            try {
                Method reset = baseTestClass.getDeclaredMethod("setup", TestContext.class);
                reset.invoke(test, testContext);
            } catch (Exception e) {
                throw new RuntimeException("setCurrentHolder Error. Caused by:" + e.getMessage(), e);
            }
        }
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        final Statement beforeClasses = super.withBeforeClasses(statement);
        return new BeforeCallbackStatement(beforeClasses, new Statement() {
            @Override
            public void evaluate() throws Throwable {
                beforeClass();
            }
        });
    }

    public void beforeClass() throws Throwable {
        logger.debug("beforeClass");
        // TODO MockApplicationContext.start();
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        final Statement afterClasses = super.withAfterClasses(statement);
        return new AfterCallbackStatement(afterClasses, new Statement() {
            @Override
            public void evaluate() throws Throwable {
                afterClass();
            }
        });
    }

    public void afterClass() throws Throwable {
        logger.debug("afterClass");
        // TODO MockApplicationContext.close()
    }
}