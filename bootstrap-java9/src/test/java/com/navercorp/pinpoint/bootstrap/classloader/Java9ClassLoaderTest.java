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

package com.navercorp.pinpoint.bootstrap.classloader;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9ClassLoaderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class slf4jClass = org.slf4j.LoggerFactory.class;

    @Test
    public void newClassLoader_bootstrap() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = new Java9ClassLoader(new URL[0], null);
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


    @Test
    public void loadClass_bootstrap() throws Exception {

        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(new URL[]{}, null);
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
