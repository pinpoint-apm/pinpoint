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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.instrument.classpool.HierarchyMultipleClassPool;
import com.navercorp.pinpoint.profiler.instrument.classpool.NamedClassPool;

import java.net.URL;
import java.net.URLClassLoader;

public class HierarchyMultipleClassPoolTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testGetClassPool() throws Exception {
        NamedClassPool cp = new NamedClassPool("test");
        HierarchyMultipleClassPool multipleClassPool = new HierarchyMultipleClassPool(cp);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        NamedClassPool classPool = multipleClassPool.getClassPool(systemClassLoader);
        CtClass string = classPool.get("java.lang.String");
        logger.debug("{}", string);
    }

    @Test
    public void logSystemClassLoader() {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        logger.debug("classLoader system:{}", systemClassLoader);

        ClassLoader ext = systemClassLoader.getParent();
        logger.debug("classLoader ext:{}", ext);

        ClassLoader boot = ext.getParent();
        logger.debug("classLoader boot:{}", boot);

        logger.debug("boot:{}", String.class.getClassLoader());

        logger.debug("system:{}", this.getClass().getClassLoader());
    }


    @Test
    public void testTestClass() throws Exception {
        NamedClassPool pool = new NamedClassPool("test");
        pool.childFirstLookup = true;

        HierarchyMultipleClassPool multipleClassPool = new HierarchyMultipleClassPool(pool);

        ClassLoader classLoader = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());

        multipleClassPool.getClassPool(classLoader);

        logger.debug("size {}", multipleClassPool.size());

        for (NamedClassPool classPool1 : multipleClassPool.values()) {
            logger.debug("classPool:{} name:{}", classPool1, classPool1.getName());
        }

        Assert.assertEquals(2, multipleClassPool.size());
    }

}