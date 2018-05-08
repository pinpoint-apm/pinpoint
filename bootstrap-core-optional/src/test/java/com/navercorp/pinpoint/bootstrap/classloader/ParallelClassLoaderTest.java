package com.navercorp.pinpoint.bootstrap.classloader;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;

/**
 * @author Taejin Koo
 */
public class ParallelClassLoaderTest {

    private final Class slf4jClass = org.slf4j.LoggerFactory.class;

    @Test
    public void testOnLoadClass() throws Exception {
        Class classLoaderType = ParallelClassLoader.class;

        ClassLoader cl = onLoadTest(classLoaderType, slf4jClass);

        close(cl);
    }

    /**
     * TODO duplicate code
     */
    private ClassLoader onLoadTest(Class classLoaderType, Class testClass) throws ClassNotFoundException {
        URL testClassJar = getJarURL(testClass);
        URL[] urls = {testClassJar};
        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(urls, Thread.currentThread().getContextClassLoader());
        Assert.assertSame(cl.getClass(), classLoaderType);

        try {
            cl.loadClass("test");
            Assert.fail();
        } catch (ClassNotFoundException ignored) {
        }

        Class selfLoadClass = cl.loadClass(testClass.getName());
        Assert.assertNotSame(testClass, selfLoadClass);
        Assert.assertSame(cl, selfLoadClass.getClassLoader());
        Assert.assertSame(testClass.getClassLoader(), this.getClass().getClassLoader());
        return cl;
    }

    private URL getJarURL(Class clazz) {
        try {
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            URL location = codeSource.getLocation();
            URL url = location.toURI().toURL();
            return url;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void close(ClassLoader classLoader) throws IOException {
        if (classLoader instanceof Closeable) {
            ((Closeable)classLoader).close();
        }
    }

    @Test
    public void testBootstrapClassLoader() throws Exception {
        ClassLoader classLoader = new ParallelClassLoader(new URL[0], null);
        close(classLoader);
    }

}
