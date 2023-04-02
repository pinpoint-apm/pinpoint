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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9ClassLoaderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Class<?> clazz = org.apache.logging.log4j.LogManager.class;

    @Test
    public void newClassLoader_bootstrap() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = new Java9ClassLoader("test", new URL[0], null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        classLoader.loadClass("java.lang.String");
        close(classLoader);
    }

    @Test
    public void testOnLoadClass() throws Exception {

        ClassLoader cl = onLoadTest(Java9ClassLoader.class, clazz);
        close(cl);
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
    public void loadClass_bootstrap() throws Exception {

        ClassLoader cl = PinpointClassLoaderFactory.createClassLoader(this.getClass().getName(), new URL[]{}, null, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        assertThat(cl).isInstanceOf(Java9ClassLoader.class);

        Class<?> stringClazz1 = cl.loadClass("java.lang.String");
        Class<?> stringClazz2 = ClassLoader.getSystemClassLoader().loadClass("java.lang.String");
        Assertions.assertSame(stringClazz1, stringClazz2, "reference");
        Assertions.assertSame(stringClazz1.getClassLoader(), stringClazz2.getClassLoader(), "classLoader");

        close(cl);
    }

    private void close(ClassLoader classLoader) throws IOException {
        if (classLoader instanceof Closeable) {
            ((Closeable) classLoader).close();
        }
    }
}
