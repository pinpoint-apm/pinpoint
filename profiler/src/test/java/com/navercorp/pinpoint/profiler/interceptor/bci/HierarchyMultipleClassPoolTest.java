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

import javassist.ClassPool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;

public class HierarchyMultipleClassPoolTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testGetClassPool() throws Exception {
        ClassPool cp = new ClassPool();
        HierarchyMultipleClassPool multipleClassPool = new HierarchyMultipleClassPool(cp);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        ClassPool classPool = multipleClassPool.getClassPool(systemClassLoader);
    }

    @Test
    public void test() {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        logger.debug("classLoader:{}", systemClassLoader);

        ClassLoader parent = systemClassLoader.getParent();
        logger.debug("classLoader parent:{}", parent);
        logger.debug("classLoader parent.parent:{}", parent.getParent());




    }


    @Test
    public void testTestClass() throws Exception {
        ClassPool pool = new ClassPool();
        pool.childFirstLookup = true;

        HierarchyMultipleClassPool multipleClassPool = new HierarchyMultipleClassPool(pool);

        ClassLoader classLoader = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());

        ClassPool classPool = multipleClassPool.getClassPool(classLoader);

        logger.debug("{}", multipleClassPool.size());

        for (ClassPool classPool1 : multipleClassPool.values()) {
            logger.debug("classPool:{}", classPool1);

            logger.debug("classPool:{}", ((NamedClassPool)classPool1).getName());
        }

    }

}