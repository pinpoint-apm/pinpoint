package com.navercorp.pinpoint.bootstrap.classloader;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Taejin Koo
 */
public class ParallelCapablePinpointURLClassLoaderTest {

    @Test
    public void testOnLoadClass() throws Exception {

        URLClassLoader cl = PinpointClassLoaderFactory.createClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());

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

        if (cl instanceof ParallelCapablePinpointURLClassLoader) {
            Assert.assertTrue(((ParallelCapablePinpointURLClassLoader) cl).onLoadClass("com.navercorp.pinpoint.profiler.DefaultAgent"));
        } else {
            Assert.fail();
        }

    }

}
