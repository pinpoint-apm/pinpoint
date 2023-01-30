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

import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

/**
 * @author Taejin Koo
 */
public class ParallelClassLoaderTest {

    private final Class<?> clazz = org.apache.logging.log4j.LogManager.class;

    @Test
    public void testOnLoadClass() throws Exception {
        Class<?> classLoaderType = ParallelClassLoader.class;

        ClassLoader cl = onLoadTest(classLoaderType, clazz);

        ClassLoaderUtils.close(cl);
    }

    /**
     * TODO duplicate code
     */
    private ClassLoader onLoadTest(Class<?> classLoaderType, Class<?> testClass) throws ClassNotFoundException {
        URL testClassJar = CodeSourceUtils.getCodeLocation(testClass);
        URL[] urls = {testClassJar};
        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(this.getClass().getName(), urls, null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        Assertions.assertSame(cl.getClass(), classLoaderType);

        Assertions.assertThrowsExactly(ClassNotFoundException.class, () -> {
            cl.loadClass("test");
        });

        Class<?> selfLoadClass = cl.loadClass(testClass.getName());
        Assertions.assertNotSame(testClass, selfLoadClass);
        Assertions.assertSame(cl, selfLoadClass.getClassLoader());
        Assertions.assertSame(testClass.getClassLoader(), this.getClass().getClassLoader());
        return cl;
    }


    @Test
    public void testBootstrapClassLoader() throws Exception {
        ClassLoader classLoader = new ParallelClassLoader(this.getClass().getName(), new URL[0], null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        ClassLoaderUtils.close(classLoader);
    }

}
