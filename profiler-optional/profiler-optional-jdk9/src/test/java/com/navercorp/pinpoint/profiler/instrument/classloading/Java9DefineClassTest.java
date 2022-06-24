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
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9DefineClassTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void defineClass() throws ClassNotFoundException, IOException {
        URL location = CodeSourceUtils.getCodeLocation(Logger.class);
        logger.debug("location:{}", location);

        URL[] empty = {};
        URLClassLoader classLoader = new URLClassLoader(empty, null);
        try {
            classLoader.loadClass(Logger.class.getName());
            Assertions.fail();
        } catch (ClassNotFoundException ignored) {
        }

        String className = JavaAssistUtils.javaClassNameToJvmResourceName(Logger.class.getName());
        InputStream classStream = Logger.class.getClassLoader().getResourceAsStream(className);
        Assertions.assertNotNull("className not found", className);

        byte[] bytes = IOUtils.toByteArray(classStream);

        DefineClass defineClass = getDefineClass();
        Class<?> defineClassSlf4jLogger = defineClass.defineClass(classLoader, Logger.class.getName(), bytes);
        Class<?> slf4jLogger = classLoader.loadClass(Logger.class.getName());

        Assertions.assertSame(defineClassSlf4jLogger, slf4jLogger);
        Assertions.assertSame(slf4jLogger.getClassLoader(), classLoader);

        Assertions.assertNotSame(slf4jLogger.getClassLoader(), Logger.class.getClassLoader());

    }

    private DefineClass getDefineClass() {
        if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            return new Java9DefineClass();
        }
        return new ReflectionDefineClass();
    }

}