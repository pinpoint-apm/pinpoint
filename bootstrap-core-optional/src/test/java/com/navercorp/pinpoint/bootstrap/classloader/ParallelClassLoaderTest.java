package com.navercorp.pinpoint.bootstrap.classloader;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/**
 * @author Taejin Koo
 */
public class ParallelClassLoaderTest {

    @Test
    public void testOnLoadClass() throws Exception {

        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());
        Assert.assertTrue(cl instanceof ParallelClassLoader);

        try {
            cl.loadClass("test");
            Assert.fail();
        } catch (ClassNotFoundException ignored) {
        }

//        try {
//            cl.loadClass("com.navercorp.pinpoint.profiler.DefaultAgent");
//        } catch (ClassNotFoundException e) {
//
//        }
        // should be able to test using the above code, but it is not possible from bootstrap testcase.
        // it could be possible by specifying the full path to the URL classloader, but it would be harder to maintain.
        // for now, just test if DefaultAgent is specified to be loaded

        if (cl instanceof ParallelClassLoader) {
            Assert.assertTrue(((ParallelClassLoader) cl).onLoadClass("com.navercorp.pinpoint.profiler.DefaultAgent"));
        } else {
            Assert.fail();
        }

        close(cl);
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
