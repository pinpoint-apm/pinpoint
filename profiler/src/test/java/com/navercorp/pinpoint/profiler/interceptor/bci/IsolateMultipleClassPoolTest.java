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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import javassist.CtClass;

import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.instrument.classpool.IsolateMultipleClassPool;
import com.navercorp.pinpoint.profiler.instrument.classpool.NamedClassPool;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;


public class IsolateMultipleClassPoolTest {

    private static final Logger logger = LoggerFactory.getLogger(IsolateMultipleClassPoolTest.class);

    private ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    private static final String TEST_CLASS = "org.slf4j.Logger";
    private static final URL TEST_JAR_URL = getLoggerUrl();

    private URLClassLoader root = new URLClassLoader(new URL[0], systemClassLoader);

    private URLClassLoader child1 = new URLClassLoader(new URL[0], root);

    private URLClassLoader child2 = new URLClassLoader(new URL[] { TEST_JAR_URL }, systemClassLoader) {
        @Override
        public InputStream getResourceAsStream(String name) {
            logger.debug("child2 getResourceAsStream() {}", name);
            if (name.equals(TEST_CLASS)) {
                final URL resource = findResource(name);
                if (resource == null) {
                    return null;
                }
                try {
                    return resource.openStream();
                } catch (IOException e) {
                    return null;
                }
            }
            return super.getResourceAsStream(name);
        }
    };

    public static URL getLoggerUrl() {
        URL location = Logger.class.getProtectionDomain().getCodeSource().getLocation();
        logger.debug("Slf4j url:{}", location);
        return location;
    }


    @Test
    public void testPoolSize() throws Exception {

        IsolateMultipleClassPool pool = new IsolateMultipleClassPool();

        NamedClassPool systemPool = pool.getClassPool(systemClassLoader);
        Assert.assertEquals(0, pool.size());
        pool.getClassPool(null);
        Assert.assertEquals(0, pool.size());
        Assert.assertSame(systemPool, pool.getClassPool(systemClassLoader));

        NamedClassPool child1Pool = pool.getClassPool(child1);
        Assert.assertEquals(1, pool.size());
        NamedClassPool child1Pool_recheck = pool.getClassPool(child1);
        Assert.assertEquals(1, pool.size());
        Assert.assertSame(child1Pool, child1Pool_recheck);
        Assert.assertNotSame(systemPool, child1Pool);


        NamedClassPool classPool2 = pool.getClassPool(child2);
        Assert.assertEquals(2, pool.size());

        pool.getClassPool(child2);
        Assert.assertEquals(2, pool.size());
        Assert.assertNotSame(systemPool, classPool2);

    }


    @Test
    public void testGetClass() throws Exception {

        IsolateMultipleClassPool pool = new IsolateMultipleClassPool();

        NamedClassPool systemPool = pool.getClassPool(systemClassLoader);

        String classname = "java.lang.String";
        CtClass systemString = systemPool.get(classname);


        NamedClassPool childPool = pool.getClassPool(child1);
        CtClass childPoolString  = childPool.get("java.lang.String");

        Assert.assertNotSame(systemString, childPoolString);
        Assert.assertNotSame(systemString.getClassPool(), childPoolString.getClassPool());

        CtClass testClass = childPool.get(this.getClass().getName());
        Assert.assertNotSame(systemString.getClassPool(), testClass.getClassPool());

        NamedClassPool childPool2 = pool.getClassPool(child2);
        CtClass testClass2 = childPool2.get(this.getClass().getName());
        Assert.assertNotSame(systemString.getClassPool(), testClass2.getClassPool());

        CtClass testCtClass = childPool2.get(TEST_CLASS);
        logger.debug("className:{}", Arrays.toString(testCtClass.getConstructors()));

    }

    @Test
    public void testGetClass_childLookupFirst() throws Exception {

        IsolateMultipleClassPool pool = new IsolateMultipleClassPool(false, IsolateMultipleClassPool.EMPTY_EVENT_LISTENER, null);

        NamedClassPool systemPool = pool.getClassPool(systemClassLoader);
        CtClass systemLogger = systemPool.get(TEST_CLASS);

        NamedClassPool rootPool = pool.getClassPool(root);
        CtClass rootLogger = rootPool.get(TEST_CLASS);
        Assert.assertSame(systemLogger, rootLogger);

        NamedClassPool childPool = pool.getClassPool(child1);
        CtClass childLogger = childPool.get(TEST_CLASS);
        Assert.assertSame(systemLogger, childLogger);

        NamedClassPool childPool2 = pool.getClassPool(child2);
        CtClass child2Logger = childPool2.get(TEST_CLASS);
        Assert.assertSame(systemLogger, child2Logger);

    }


}