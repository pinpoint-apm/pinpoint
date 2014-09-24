package com.nhn.pinpoint.profiler.junit4;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.DummyInstrumentation;
import com.nhn.pinpoint.profiler.context.DefaultTrace;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 * @author hyungil.jeong
 */
public final class PinpointJUnit4ClassRunner extends BlockJUnit4ClassRunner {

    private static final Logger logger = LoggerFactory.getLogger(PinpointJUnit4ClassRunner.class);
    private static final Class<TestClassLoader> defaultTestClassLoader = TestClassLoader.class;

    private final TestClassLoader testClassLoader;
    private final TestContext testContext;
    private final DefaultAgent testAgent;

    public PinpointJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("PinpointJUnit4ClassRunner constructor called with [" + clazz + "].");
        }
        this.testAgent = createTestAgent();
        this.testClassLoader = getTestClassLoader(clazz);
        this.testClassLoader.initialize();
        try {
            this.testContext = new TestContext(this.testClassLoader, clazz);
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
        // 테스트 대상을 TestClassLoader로 로드된 테스트 객체로 바꿔치기 한다.
        // JUnit Runner에서 내부적으로 getTestClass()를 호출하여 사용하는데 이게 final이어서 override 불가.
        try {
            // PinpointJunit4ClassRunner -> BlockJUnit4ClassRunner -> ParentRunner.fTestClass
            Field testClassField = this.getClass().getSuperclass().getSuperclass().getDeclaredField("fTestClass");
            testClassField.setAccessible(true);
            testClassField.set(this, this.testContext.getTestClass());
        } catch (Exception e) {
            // InitializationError로 퉁치자.
            throw new InitializationError(e);
        }
    }

    private DefaultAgent createTestAgent() throws InitializationError {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        ProfilerConfig profilerConfig = new ProfilerConfig();

        String path = MockAgent.class.getClassLoader().getResource("pinpoint.config").getPath();
        try {
            profilerConfig.readConfigFile(path);
        } catch (IOException e) {
            throw new InitializationError("Unable to read pinpoint.config");
        }

        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        return new MockAgent("", new DummyInstrumentation(), profilerConfig);
    }

    private Class<?> findPinpointTestClassLoaderAnnotationForClass(Class<?> testClass) {
        if (testClass == null || testClass.equals(Object.class)) {
            return null;
        }
        if (testClass.isAnnotationPresent(PinpointTestClassLoader.class)) {
            return testClass;
        }
        return findPinpointTestClassLoaderAnnotationForClass(testClass.getSuperclass());
    }

    private TestClassLoader getTestClassLoader(Class<?> testClass) throws InitializationError {
        Class<?> classWithPinpointTestClassLoaderAnnotationSpecified = findPinpointTestClassLoaderAnnotationForClass(testClass); 
        if (classWithPinpointTestClassLoaderAnnotationSpecified == null) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("@PinpointTestClassLoader not found for class [%s]", testClass));
            }
            return createTestClassLoader(defaultTestClassLoader);
        } else {
            PinpointTestClassLoader pinpointTestClassLoader = classWithPinpointTestClassLoaderAnnotationSpecified.getAnnotation(PinpointTestClassLoader.class);
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("Retrieved @PinpointTestClassLoader [%s] for class [%s]", pinpointTestClassLoader, testClass));
            }
            return createTestClassLoader(pinpointTestClassLoader.value());
        }
    }

    private <T extends TestClassLoader> TestClassLoader createTestClassLoader(Class<T> testClassLoader) throws InitializationError {
        try {
            Constructor<T> c = testClassLoader.getConstructor(DefaultAgent.class);
            T classLoader = c.newInstance(this.testAgent);
            return classLoader;
        } catch (Exception e) {
            // 어떤 exception이 발생하던 결국 InitializationError.
            throw new InitializationError("Error instantiating Test");
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        TraceContext traceContext = this.testAgent.getTraceContext();
        beginTracing(traceContext);
        final Thread thread = Thread.currentThread();
        final ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(this.testClassLoader);
            super.runChild(method, notifier);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
            endTracing(traceContext);
        }
    }

    // TODO refine root trace parameters for test
    private void beginTracing(TraceContext traceContext) {
        Trace trace = traceContext.newTraceObject();
        trace.markBeforeTime();
        trace.recordServiceType(ServiceType.TEST);
    }

    private void endTracing(TraceContext traceContext) {
        try {
            Trace trace = traceContext.currentRawTraceObject();
            try {
                trace.markAfterTime();
            } finally {
                trace.traceRootBlockEnd();
            }
        } finally {
            traceContext.detachTraceObject();
        }
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        // TestContext의 baseTestClass는 BasePinpointTest이므로, 캐스팅해도 된다.
        @SuppressWarnings("unchecked")
        Class<BasePinpointTest> baseTestClass = (Class<BasePinpointTest>)this.testContext.getBaseTestClass();
        if (baseTestClass.isInstance(test)) {
            DefaultTrace currentTrace = (DefaultTrace)this.testAgent.getTraceContext().currentRawTraceObject();
            Method[] methods = baseTestClass.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals("setCurrentStorage")) {
                    try {
                        m.setAccessible(true);
                        m.invoke(test, currentTrace.getStorage());
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

}
