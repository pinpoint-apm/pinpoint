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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;

/**
 * @author Woonduk Kang(emeroad)
 */
final class Java9DefineClass implements DefineClass {
    private static final Object OBJECT = JavaLangAccessHelper.getJavaLangAccessObject();
    private static final MethodHandle DEFINE_CLASS_METHOD_HANDLE = JavaLangAccessHelper.getDefineClassMethodHandle();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("define class:{} cl:{}", name, classLoader);
        }

        try {
            return (Class<?>) DEFINE_CLASS_METHOD_HANDLE.invoke(OBJECT, classLoader, name, bytes, null, null);
        } catch (Throwable e) {
            logger.warn("{} define fail cl:{} Caused by:{}", name, classLoader, e.getMessage(), e);
            throw new RuntimeException(name + " define fail Caused by:" + e.getMessage(), e);
        }
    }
}
