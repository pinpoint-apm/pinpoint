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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9BootLoaderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testFindResource() {

        Java9BootLoader bootLoader = new Java9BootLoader();
        String stringResource = getInternalName(String.class);

        URL url = bootLoader.findResource(stringResource);
        Assert.assertNotNull(url);
    }

    private static String getInternalName(Class<?> clazz) {
        String name = clazz.getName();
        return name.replace('.', '/').concat(".class");
    }

    @Test
    public void testFindResources() throws IOException {
        Java9BootLoader bootLoader = new Java9BootLoader();
        String stringResource = getInternalName(String.class);

        Enumeration<URL> bootstrapResources = bootLoader.findResources(stringResource);
        List<URL> list = Collections.list(bootstrapResources);
        Assert.assertEquals(1, list.size());
        logger.debug("list:{}", list);
    }
}