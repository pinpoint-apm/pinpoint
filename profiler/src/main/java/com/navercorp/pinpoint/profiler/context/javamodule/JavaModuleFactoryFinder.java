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

package com.navercorp.pinpoint.profiler.context.javamodule;

import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class JavaModuleFactoryFinder {
    private JavaModuleFactoryFinder() {
    }

    public static JavaModuleFactory lookup(Instrumentation instrumentation) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }

        final Class<JavaModuleFactory> javaModuleFactory = getJavaModuleFactory();
        try {
            Constructor<JavaModuleFactory> constructor = javaModuleFactory.getDeclaredConstructor(Instrumentation.class);
            return constructor.newInstance(instrumentation);
        } catch (Exception e) {
            throw new IllegalStateException("JavaModuleFactory() invoke fail Caused by:" + e.getMessage(), e);
        }
    }

    private static Class<JavaModuleFactory> getJavaModuleFactory() {
        final String factoryName = "com.navercorp.pinpoint.bootstrap.java9.module.DefaultJavaModuleFactory";
        try {
            return (Class<JavaModuleFactory>) Class.forName(factoryName, false, JavaModule.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(factoryName + " not found");
        }
    }
}
