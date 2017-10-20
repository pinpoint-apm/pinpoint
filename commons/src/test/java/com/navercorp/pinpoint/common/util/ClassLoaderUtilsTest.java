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

package com.navercorp.pinpoint.common.util;

import org.junit.After;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final URLClassLoader FAKE_CLASS_LOADER = new URLClassLoader(new URL[0]);

    private static final ClassLoaderUtils.ClassLoaderCallable FAKE_CLASS_LOADER_CALLABLE = new ClassLoaderUtils.ClassLoaderCallable() {
        @Override
        public ClassLoader getClassLoader() {
            return FAKE_CLASS_LOADER;
        }
    };

    private ClassLoader beforeSetupClassLoader;

    @Before
    public void setUp() throws Exception {
        Thread currentThread = Thread.currentThread();
        beforeSetupClassLoader = currentThread.getContextClassLoader();
    }

    @After
    public void tearDown() throws Exception {
        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(beforeSetupClassLoader);
    }

    @Test
    public void testGetClassLoader1() throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader contextClassLoader = thread.getContextClassLoader();

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();

        Assert.assertSame(contextClassLoader, classLoader);
    }

    @Test
    public void testGetClassLoader2() throws Exception {
        final Thread currentThread = Thread.currentThread();

        currentThread.setContextClassLoader(FAKE_CLASS_LOADER);
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();

        Assert.assertSame(classLoader, FAKE_CLASS_LOADER);
    }

    @Test
    public void testGetClassLoader3() throws Exception {
        final Thread currentThread = Thread.currentThread();

        currentThread.setContextClassLoader(null);

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader(FAKE_CLASS_LOADER_CALLABLE);

        Assert.assertSame(classLoader, FAKE_CLASS_LOADER);

    }

    @Test
    public void append() throws Exception {
        String log = ClassLoaderUtils.dumpStandardClassLoader();
        logger.debug("StandardClassLoader dump:{}", log);
    }
}