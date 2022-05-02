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


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author Woonduk Kang(emeroad)
 */
final class Java9DefineClass implements DefineClass {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("define class:{} cl:{}", name, classLoader);
        }
        final JavaLangAccess javaLangAccess = JavaLangAccessHelper.getJavaLangAccess();
        try {
            return javaLangAccess.defineClass(classLoader, name, bytes, null, null);
        } catch (Throwable e) {
            logger.warn("{} define fail cl:{} Caused by:{}", name, classLoader, e.getMessage(), e);
            throw new RuntimeException(name + " define fail Caused by:" + e.getMessage(), e);
        }
    }
}
