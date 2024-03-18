/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.test.classloader;

import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * @author hyungil.jeong
 */
public class TestClassLoaderFactory {

    private static final Logger LOGGER = LogManager.getLogger(TestClassLoaderFactory.class);

    public static TestClassLoader createTestClassLoader(DefaultApplicationContext applicationContext, URL[] urls, ClassLoader parentClassLoader) {
//        final TestClassLoader testClassLoader = new TestClassLoader(applicationContext, urls, parentClassLoader);
        final TestClassLoader testClassLoader = new TestClassLoader(applicationContext, urls);
        return testClassLoader;
    }

}
