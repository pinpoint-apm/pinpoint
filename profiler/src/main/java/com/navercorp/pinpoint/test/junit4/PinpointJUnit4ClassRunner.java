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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import org.apache.thrift.TBase;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.navercorp.pinpoint.test.MockAgent;
import com.navercorp.pinpoint.test.PeekableDataSender;
import com.navercorp.pinpoint.test.ResettableServerMetaDataHolder;
import com.navercorp.pinpoint.test.TestClassLoader;
import com.navercorp.pinpoint.test.TestClassLoaderFactory;

/**
 * @author hyungil.jeong
 */
public final class PinpointJUnit4ClassRunner extends BlockJUnit4ClassRunner implements StatementCallback {

    private static final Logger logger = LoggerFactory.getLogger(PinpointJUnit4ClassRunner.class);

    private TestClassLoader testClassLoader;
    private TestContext testContext;
    private MockAgent testAgent;
    private final PLoggerBinder loggerBinder = new Slf4jLoggerBinder();

    public PinpointJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("PinpointJUnit4ClassRunner constructor called with [{}].", clazz);
        }
    }

    private void beforeTestClass() {
        try {
            this.testAgent = createMockAgent();
            this.testClassLoader = getTestClassLoader();
            this.testClassLoader.initialize();
            this.testContext = new TestContext(this.testClassLoader);
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    protected TestClass createTestClass(Class<?> testClass) {
        logger.debug("createTestClass {}", testClass);
        beforeTestClass();
        return testContext.createTestClass(testClass);
    }


    private MockAgent createMockAgent() throws InitializationError {
        PLoggerFactory.initialize(loggerBinder);
        logger.trace("agent create");
        try {
            return MockAgent.of("pinpoint.config");
        } catch (IOException e) {
            throw new InitializationError("Unable to read pinpoint.config");
        }
    }
    
    private TestClassLoader getTestClassLoader() {
        return TestClassLoaderFactory.createTestClassLoader(this.testAgent);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        System.out.println("runChild start---------------------");
        beginTracing(method);
        final Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(this.testClassLoader);
            super.runChild(method, notifier);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
            endTracing(method, notifier);
            System.out.println("runChild end===================");
        }
    }

    private void beginTracing(FrameworkMethod method) {
        if (shouldCreateNewTraceObject(method)) {
            TraceContext traceContext = this.testAgent.getTraceContext();
            Trace trace = traceContext.newTraceObject();
            trace.markBeforeTime();
            trace.recordServiceType(ServiceType.TEST);
        }
    }

    private void endTracing(FrameworkMethod method, RunNotifier notifier) {
        if (shouldCreateNewTraceObject(method)) {
            TraceContext traceContext = this.testAgent.getTraceContext();
            try {
                Trace trace = traceContext.currentRawTraceObject();
                if (trace == null) {
                    // Trace is already detached from the ThreadLocal storage.
                    // Happens when root trace method is tested without @IsRootSpan.
                    EachTestNotifier testMethodNotifier = new EachTestNotifier(notifier, super.describeChild(method));
                    String traceObjectAlreadyDetachedMessage = "Trace object already detached. If you're testing a trace root, please add @IsRootSpan to the test method";
                    testMethodNotifier.addFailure(new IllegalStateException(traceObjectAlreadyDetachedMessage));
                } else {
                    try {
                        trace.markAfterTime();
                    } finally {
                        trace.traceRootBlockEnd();
                    }
                }
            } finally {
                traceContext.detachTraceObject();
            }
        }
    }

    private boolean shouldCreateNewTraceObject(FrameworkMethod method) {
        IsRootSpan isRootSpan = method.getAnnotation(IsRootSpan.class);
        if (isRootSpan == null || !isRootSpan.value()) {
            return true;
        }
        return false;
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {

        // It's safe to cast
        @SuppressWarnings("unchecked")
        Class<BasePinpointTest> baseTestClass = (Class<BasePinpointTest>)this.testContext.getBaseTestClass();
        if (baseTestClass.isInstance(test)) {
            Method[] methods = baseTestClass.getDeclaredMethods();
            for (Method m : methods) {
                // Inject testDataSender into the current Test instance. 
                if (m.getName().equals("setCurrentHolder")) {
                    try {
                        // reset PeekableDataSender for each test method
                        this.testAgent.getPeekableSpanDataSender().clear();
                        m.setAccessible(true);
                        m.invoke(test, this.testAgent.getPeekableSpanDataSender());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                // Inject serverMetaDataHolder into the current Test instance.
                if (m.getName().equals("setServerMetaDataHolder")) {
                    try {
                        ResettableServerMetaDataHolder serverMetaDataHolder = (ResettableServerMetaDataHolder)this.testAgent.getTraceContext().getServerMetaDataHolder();
//                        serverMetaDataHolder.reset();
                        m.setAccessible(true);
                        m.invoke(test, serverMetaDataHolder);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return super.methodInvoker(method, test);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        final Statement beforeClasses = super.withBeforeClasses(statement);
        return new BeforeCallbackStatement(beforeClasses, this);
    }


    @Override
    public void before() throws Throwable {
        logger.debug("beforeClass");
    }

    @Override
    public void after() throws Throwable {
        logger.debug("afterClass");
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        final Statement afterClasses = super.withAfterClasses(statement);
        return new AfterCallbackStatement(afterClasses, this);
    }

}
