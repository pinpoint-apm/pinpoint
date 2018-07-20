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

/**
 * @author Woonduk Kang(emeroad)
 */
final class Java9DefineClass implements DefineClass {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("define class:{} cl:{}", name, classLoader);
        }
        return jdk.internal.misc.SharedSecrets.getJavaLangAccess().defineClass(classLoader, name, bytes, null, null);
    }
}
