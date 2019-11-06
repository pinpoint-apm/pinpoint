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

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9DefineClassTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void defineClass() throws ClassNotFoundException, IOException {
        URL location = CodeSourceUtils.getCodeLocation(Logger.class);
        logger.debug("location:{}", location);

        URL[] empty = {};
        URLClassLoader classLoader = new URLClassLoader(empty, null);
        try {
            classLoader.loadClass(Logger.class.getName());
            Assert.fail();
        } catch (ClassNotFoundException ignore) {
        }

        String className = JavaAssistUtils.javaClassNameToJvmResourceName(Logger.class.getName());
        InputStream classStream = Logger.class.getClassLoader().getResourceAsStream(className);
        byte[] bytes = IOUtils.readFully(classStream, classStream.available());

        DefineClass defineClass = new Java9DefineClass();
        Class<?> defineClassSlf4jLogger = defineClass.defineClass(classLoader, Logger.class.getName(), bytes);
        Class<?> slf4jLogger = classLoader.loadClass(Logger.class.getName());

        Assert.assertSame(defineClassSlf4jLogger, slf4jLogger);
        Assert.assertSame(slf4jLogger.getClassLoader(), classLoader);

        Assert.assertNotSame(slf4jLogger.getClassLoader(), Logger.class.getClassLoader());

    }

}