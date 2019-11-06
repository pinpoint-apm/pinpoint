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

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

import java.lang.reflect.Constructor;

/**
 * @author Woonduk Kang(emeroad)
 */
final class DefineClassFactory {
    private static final DefineClass defineClass = newDefineClass();

    private static DefineClass newDefineClass() {
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            final ClassLoader agentClassLoader = DefineClassFactory.class.getClassLoader();
            final String name = "com.navercorp.pinpoint.profiler.instrument.classloading.Java9DefineClass";
            try {
                Class<DefineClass> defineClassClazz = (Class<DefineClass>) agentClassLoader.loadClass(name);
                Constructor<DefineClass> constructor = defineClassClazz.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(name + " create fail Caused by:" + e.getMessage(), e);
            }
        }

        return new ReflectionDefineClass();
    }

    static DefineClass getDefineClass() {
        return defineClass;
    }

}
