/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.classloader;

import com.navercorp.pinpoint.test.MockApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.ClassUtils;

/**
 * @author hyungil.jeong
 */
public class TestClassLoaderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClassLoaderFactory.class);

    // Classes to check for to determine which Clover runtime has been loaded AFTER source code instrumentation.
    private static final String CENQUA_CLOVER = "com_cenqua_clover.Clover";

    private static final String ATLASSIAN_CLOVER = "com_atlassian_clover.Clover";

    public static TestClassLoader createTestClassLoader(MockApplicationContext applicationContext) {
        final TestClassLoader testClassLoader = new TestClassLoader(applicationContext);
        addCloverPackage(testClassLoader, CENQUA_CLOVER);
        addCloverPackage(testClassLoader, ATLASSIAN_CLOVER);
        return testClassLoader;
    }


    private static void addCloverPackage(TestClassLoader testClassLoader, String className) {
        String packageName = getPackageName(className);
        testClassLoader.addDelegateClass(packageName);
    }

    private static String getPackageName(String className) {
        return ClassUtils.getPackageName(className) + ".";
    }

}
