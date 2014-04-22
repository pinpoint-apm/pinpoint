package com.nhn.pinpoint.profiler.junit4;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
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
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 * @author Hyun Jeong
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
	}

	private DefaultAgent createTestAgent() throws InitializationError {
		System.setProperty("catalina.home", "test");
		PLoggerFactory.initialize(new Slf4jLoggerBinder());
		
		ProfilerConfig profilerConfig = new ProfilerConfig();
		
		String path = ProfilerConfig.class.getClassLoader().getResource("pinpoint.config").getPath();
		try {
			profilerConfig.readConfigFile(path);
		} catch (IOException e) {
			throw new InitializationError("Unable to read pinpoint.config");
		}
		
		profilerConfig.setApplicationServerType(ServiceType.STAND_ALONE);
		return new MockAgent("", new DummyInstrumentation(), profilerConfig);
	}
	
	private final TestClassLoader getTestClassLoader(Class<?> testClass) throws InitializationError {
		PinpointTestClassLoader pinpointTestClassLoader = testClass.getAnnotation(PinpointTestClassLoader.class);
		if (pinpointTestClassLoader == null) {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("@PinpointTestClassLoader not found for class [%s]", testClass));
			}
			return createTestClassLoader(defaultTestClassLoader);
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Retrieved @PinpointTestClassLoader [%s] for class [%s]", pinpointTestClassLoader, testClass));
			}
			return createTestClassLoader(pinpointTestClassLoader.value());
		}
	}
	
	private final <T extends TestClassLoader> TestClassLoader createTestClassLoader(Class<T> testClassLoader) throws InitializationError {
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
		boolean methodHasTraceSupport = hasTraceSupport(method);
		if (methodHasTraceSupport) {
			beginTracing(traceContext);
		}
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.testClassLoader);
			super.runChild(method, notifier);
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
			if (methodHasTraceSupport) {
				endTracing(traceContext);
			}
		}
	}
	
	// TODO check annotation and return accordingly
	private boolean hasTraceSupport(FrameworkMethod method) {
		return true;
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
	protected Statement methodBlock(FrameworkMethod frameworkMethod) {
		try {
			Method newMethod = this.testContext.getTestClass().getJavaClass().getMethod(frameworkMethod.getName());
			FrameworkMethod newFrameworkMethod = new FrameworkMethod(newMethod);
			return super.methodBlock(newFrameworkMethod);
		} catch (NoSuchMethodException e) {
			return new Fail(e);
		} catch (SecurityException e) {
			return new Fail(e);
		}
	}

	@Override
	protected Object createTest() throws Exception {
		return this.testContext.getTestClass().getJavaClass().newInstance();
	}
	
	@Override
	@Deprecated
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
		@SuppressWarnings("unchecked")
		List<FrameworkMethod> befores = this.testContext.getTestClass().getAnnotatedMethods((Class<? extends Annotation>)this.testContext.getBeforeClass());
		return new RunBefores(statement, befores, target);
	}

	@Override
	@Deprecated
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
		@SuppressWarnings("unchecked")
		List<FrameworkMethod> afters = this.testContext.getTestClass().getAnnotatedMethods((Class<? extends Annotation>)this.testContext.getAfterClass());
		return new RunAfters(statement, afters, target);
	}

}
