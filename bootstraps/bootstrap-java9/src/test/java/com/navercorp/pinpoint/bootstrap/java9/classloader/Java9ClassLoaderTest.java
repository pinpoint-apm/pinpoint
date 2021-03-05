/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.java9.classloader;

import com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory;
import com.navercorp.pinpoint.bootstrap.classloader.ProfilerLibs;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9ClassLoaderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class slf4jClass = org.slf4j.LoggerFactory.class;

    @Test
    public void newClassLoader_bootstrap() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = new Java9ClassLoader("test", new URL[0], null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        classLoader.loadClass("java.lang.String");
        close(classLoader);
    }

    @Test
    public void testOnLoadClass() throws Exception {

        ClassLoader cl = onLoadTest(Java9ClassLoader.class, slf4jClass);
        close(cl);
    }

    /**
     * TODO duplicate code
     */
    private ClassLoader onLoadTest(Class<?> classLoaderType, Class testClass) throws ClassNotFoundException {
        URL testClassJar = CodeSourceUtils.getCodeLocation(testClass);
        URL[] urls = {testClassJar};
        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(this.getClass().getName(), urls, null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
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



    @Test
    public void loadClass_bootstrap() throws Exception {

        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(this.getClass().getName(), new URL[]{}, null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        Assert.assertTrue(cl instanceof Java9ClassLoader);

        Class<?> stringClazz1 = cl.loadClass("java.lang.String");
        Class<?> stringClazz2 = ClassLoader.getSystemClassLoader().loadClass("java.lang.String");
        Assert.assertSame("reference", stringClazz1, stringClazz2);
        Assert.assertSame("classLoader", stringClazz1.getClassLoader(), stringClazz2.getClassLoader());

        close(cl);
    }

    private void close(ClassLoader classLoader) throws IOException {
        if (classLoader instanceof Closeable) {
            ((Closeable) classLoader).close();
        }
    }
}
