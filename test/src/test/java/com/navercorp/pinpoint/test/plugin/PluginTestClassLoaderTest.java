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

package com.navercorp.pinpoint.test.plugin;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class PluginTestClassLoaderTest {

    @Test
    public void testNullJars() throws Exception {
        final ClassLoader classLoader = PluginTestClassLoader.getClassLoader(null);
        final Class<?> object = classLoader.loadClass("java.lang.Object");
        final Method toString = object.getMethod("toString");
        assertThat(object, notNullValue());
        assertThat(toString, notNullValue());
    }

    @Test
    public void testEmptyJars() throws Exception {
        final ClassLoader classLoader = PluginTestClassLoader.getClassLoader(Collections.<File>emptyList());
        final Class<?> object = classLoader.loadClass("java.lang.Object");
        final Method toString = object.getMethod("toString");
        assertThat(object, notNullValue());
        assertThat(toString, notNullValue());
    }

    @Test
    public void testParentClassLoaderApplied() throws Exception {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        final ClassLoader classLoader = new PluginTestClassLoader(new URL[]{}, systemClassLoader);
        final Class<?> object = classLoader.loadClass("java.lang.Object");
        final Method toString = object.getMethod("toString");
        assertThat(object, notNullValue());
        assertThat(toString, notNullValue());
    }
}