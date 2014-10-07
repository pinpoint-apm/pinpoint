package com.nhn.pinpoint.profiler.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.ClassUtils;
import com.nhn.pinpoint.profiler.DefaultAgent;

/**
 * @author hyungil.jeong
 */
public class TestClassLoaderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClassLoaderFactory.class);

    // Classes to check for to determine which Clover runtime has been loaded after source code instrumentation.
    private static final String CENQUA_CLOVER = "com_cenqua_clover.Clover";
    private static final String ATLASSIAN_CLOVER = "com_atlassian_clover.Clover";

    public static TestClassLoader createTestClassLoader(final DefaultAgent testAgent) {
        if (isCloverRuntimePresent(CENQUA_CLOVER)) {
            return createTestClassLoaderForClover(testAgent, ClassUtils.getPackageName(CENQUA_CLOVER));
        } else if (isCloverRuntimePresent(ATLASSIAN_CLOVER)) {
            return createTestClassLoaderForClover(testAgent, ClassUtils.getPackageName(ATLASSIAN_CLOVER));
        } else {
            return createDefaultTestClassLoader(testAgent);
        }
    }

    private static boolean isCloverRuntimePresent(String cloverFqcnToCheckFor) {
        return ClassUtils.isLoaded(cloverFqcnToCheckFor);
    }

    private static TestClassLoaderForClover createTestClassLoaderForClover(DefaultAgent testAgent, String cloverPackageName) {
        LOGGER.info(String.format("CloverRuntime detected. Creating CloverRuntimeTestClassLoader delegating package [%s]", cloverPackageName));
        try {
            Constructor<TestClassLoaderForClover> c = TestClassLoaderForClover.class.getConstructor(DefaultAgent.class, String.class);
            return c.newInstance(testAgent, cloverPackageName);
        } catch (Exception e) {
            handleReflectionException(e);
        }
        throw new IllegalStateException("Should not get here");
    }

    private static TestClassLoader createDefaultTestClassLoader(DefaultAgent testAgent) {
        try {
            Constructor<TestClassLoader> c = TestClassLoader.class.getConstructor(DefaultAgent.class);
            return c.newInstance(testAgent);
        } catch (Exception e) {
            handleReflectionException(e);
        }
        throw new IllegalStateException("Should not get here");
    }
    
    private static void handleReflectionException(Exception e) {
        if (e instanceof NoSuchMethodException) {
            throw new IllegalStateException("Constructor not found: " + e.getMessage());
        }
        if (e instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access constructor: " + e.getMessage());
        }
        if (e instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException)e);
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
        }
        throw new UndeclaredThrowableException(e);
    }
    
    private static void handleInvocationTargetException(InvocationTargetException e) {
        Throwable targetException = e.getTargetException();
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException)targetException;
        }
        if (targetException instanceof Error) {
            throw (Error)targetException;
        }
        throw new UndeclaredThrowableException(targetException);
    }
}
