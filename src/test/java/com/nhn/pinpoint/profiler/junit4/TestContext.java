package com.nhn.pinpoint.profiler.junit4;

import org.junit.runners.model.TestClass;

import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 * @author hyungil.jeong
 */
public class TestContext {

    private final TestClass testClass;
    private final Object baseTestClass;

    <T extends TestClassLoader> TestContext(final T testClassLoader, Class<?> clazz) throws ClassNotFoundException {
        this.testClass = new TestClass(testClassLoader.loadClass(clazz.getName()));
        this.baseTestClass = testClassLoader.loadClass(BasePinpointTest.class.getName());
    }

    public TestClass getTestClass() {
        return this.testClass;
    }

    public Object getBaseTestClass() {
        return this.baseTestClass;
    }
}
