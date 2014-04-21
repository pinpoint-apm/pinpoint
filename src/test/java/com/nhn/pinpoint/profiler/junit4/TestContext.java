package com.nhn.pinpoint.profiler.junit4;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.model.TestClass;

import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 *
 * @author Hyun Jeong
 */
public class TestContext {

	private final TestClass testClass;
	private final Object beforeClass;
	private final Object afterClass;
	
	TestContext(final TestClassLoader testClassLoader, Class<?> clazz) throws ClassNotFoundException {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(testClassLoader);
			this.testClass = new TestClass(testClassLoader.loadClass(clazz.getName()));
			this.beforeClass = testClassLoader.loadClass(Before.class.getName());
			this.afterClass = testClassLoader.loadClass(After.class.getName());
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	public TestClass getTestClass() {
		return testClass;
	}

	public Object getBeforeClass() {
		return beforeClass;
	}

	public Object getAfterClass() {
		return afterClass;
	}
}
