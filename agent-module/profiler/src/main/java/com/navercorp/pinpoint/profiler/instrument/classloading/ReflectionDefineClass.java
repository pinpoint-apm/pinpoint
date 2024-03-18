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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
final class ReflectionDefineClass implements DefineClass {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Method DEFINE_CLASS;
    static {
        try {
            DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_CLASS.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access ClassLoader.defineClass(String, byte[], int, int)", e);
        }
    }

    @Override
    public Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("define class:{} cl:{}", name, classLoader);
        }
        try {
            return (Class<?>) DEFINE_CLASS.invoke(classLoader, name, bytes, 0, bytes.length);
        } catch (ReflectiveOperationException e) {
            throw handleDefineClassFail(classLoader, name, e);
        }
    }

    private RuntimeException handleDefineClassFail(ClassLoader classLoader, String className, Exception e) {
        logger.warn("{} define fail cl:{} Caused by:{}", className, classLoader, e.getMessage(), e);
        return new RuntimeException(className + " define fail Caused by:" + e.getMessage(), e);
    }


}
