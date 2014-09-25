package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderUtilsTest {

    private static final URLClassLoader FAKE_CLASS_LOADER = new URLClassLoader(new URL[0]);

    private static final ClassLoaderUtils.ClassLoaderCallable FAKE_CLASS_LOADER_CALLABLE = new ClassLoaderUtils.ClassLoaderCallable() {
        @Override
        public ClassLoader getClassLoader() {
            return FAKE_CLASS_LOADER;
        }
    };

    @Test
    public void testGetClassLoader1() throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader contextClassLoader = thread.getContextClassLoader();

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();

        Assert.assertSame(contextClassLoader, classLoader);
    }

    @Test
    public void testGetClassLoader2() throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader old = Thread.currentThread().getContextClassLoader();

        thread.setContextClassLoader(FAKE_CLASS_LOADER);
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        try {
            Assert.assertSame(classLoader, FAKE_CLASS_LOADER);
        } finally {
            thread.setContextClassLoader(old);
        }
    }

    @Test
    public void testGetClassLoader3() throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader old = thread.getContextClassLoader();

        thread.setContextClassLoader(null);

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader(FAKE_CLASS_LOADER_CALLABLE);
        try {
            Assert.assertSame(classLoader, FAKE_CLASS_LOADER);
        } finally {
            thread.setContextClassLoader(old);
        }
    }
}