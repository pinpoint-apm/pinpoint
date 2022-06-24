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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootLoaderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testFindResource() {
        BootLoader bootLoader = newBootLoader();
        String stringResource = getInternalName(String.class);

        URL url = bootLoader.findResource(stringResource);
        Assertions.assertNotNull(url);
    }

    private BootLoader newBootLoader() {
        return BootLoaderFactory.newBootLoader();
    }

    private String getInternalName(Class<?> clazz) {
        String name = clazz.getName();
        return name.replace('.', '/').concat(".class");
    }

    @Test
    public void testFindResources() throws IOException {
        BootLoader bootLoader = newBootLoader();
        String stringResource = getInternalName(String.class);

        Enumeration<URL> bootstrapResources = bootLoader.findResources(stringResource);
        List<URL> list = Collections.list(bootstrapResources);
        Assertions.assertEquals(1, list.size());
        logger.debug("list:{}", list);
    }

    @Test
    public void testBootstrapClassLoader() throws Exception {
        BootLoader bootLoader = newBootLoader();

        ClassLoader parent = Object.class.getClassLoader();
        ClassLoader classLoader = new URLClassLoader(new URL[0], parent);
        Class<?> string1 = bootLoader.findBootstrapClassOrNull(classLoader, "java.lang.String");
        Class<?> string2 = Class.forName("java.lang.String", false, parent);

        Assertions.assertNotNull(string1);
        Assertions.assertNotNull(string2);
        Assertions.assertSame(string1, string2);
        Assertions.assertSame(string1.getClassLoader(), string2.getClassLoader());
        this.getClass().getClassLoader();

        ClassLoaderUtils.close(classLoader);
    }
}