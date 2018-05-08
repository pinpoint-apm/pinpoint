/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.net.URL;
import java.security.CodeSource;

/**
 * @author emeroad
 */
public class PinpointClassLoaderTest {

    private final Class slf4jClass = org.slf4j.LoggerFactory.class;

    @Test
    public void testOnLoadClass() throws Exception {
        ClassLoader classLoader = onLoadTest(Java6ClassLoader.class, slf4jClass);

        ClassLoaderUtils.close(classLoader);
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
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
