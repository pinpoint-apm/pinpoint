/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class MavenDependencyResolverClassLoaderTest {

    @Test
    public void testNullJars() throws Exception {
        final ClassLoader classLoader = MavenDependencyResolverClassLoader.getClassLoader(null);
        final Class<?> object = classLoader.loadClass("java.lang.Object");
        final Method toString = object.getMethod("toString");
        assertThat(object, notNullValue());
        assertThat(toString, notNullValue());
    }

    @Test
    public void testEmptyJars() throws Exception {
        final ClassLoader classLoader = MavenDependencyResolverClassLoader.getClassLoader(new String[] {});
        final Class<?> object = classLoader.loadClass("java.lang.Object");
        final Method toString = object.getMethod("toString");
        assertThat(object, notNullValue());
        assertThat(toString, notNullValue());
    }

    @Test
    public void testParentClassLoaderApplied() throws Exception {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        final ClassLoader classLoader = new MavenDependencyResolverClassLoader(new URL[]{}, systemClassLoader);
        final Class<?> object = classLoader.loadClass("java.lang.Object");
        final Method toString = object.getMethod("toString");
        assertThat(object, notNullValue());
        assertThat(toString, notNullValue());
    }
}