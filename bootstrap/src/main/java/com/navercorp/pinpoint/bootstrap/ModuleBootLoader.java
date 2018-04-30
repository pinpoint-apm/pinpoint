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

package com.navercorp.pinpoint.bootstrap;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
class ModuleBootLoader {

    private final Instrumentation instrumentation;
    // @Nullable
    private final ClassLoader parentClassLoader;

    ModuleBootLoader(Instrumentation instrumentation, ClassLoader parentClassLoader) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        this.instrumentation = instrumentation;
        this.parentClassLoader = parentClassLoader;
    }

    ClassLoader getPlatformClassLoader() {
        try {
            Method getPlatformClassLoader = ClassLoader.class.getDeclaredMethod("getPlatformClassLoader");
            return (ClassLoader) getPlatformClassLoader.invoke(ClassLoader.class);
        } catch (Exception ex) {
            throw new IllegalStateException("getPlatformClassLoader() fail Caused by:" +ex.getMessage(), ex);
        }
    }

    void loadModuleSupport() {
        try {
            Class<?> bootStrapClass = getModuleSupportFactoryClass(parentClassLoader);
            Object moduleSupportFactory = newModuleSupportFactory(bootStrapClass);

            Method newModuleSupportMethod = moduleSupportFactory.getClass().getMethod("newModuleSupport", Instrumentation.class);
            Object moduleSupport = newModuleSupportMethod.invoke(moduleSupportFactory, instrumentation);

            Class<?> moduleSupportSetup = moduleSupport.getClass();
            Method setupMethod = moduleSupportSetup.getMethod("setup");
            setupMethod.invoke(moduleSupport);
        } catch (Exception e) {
            System.out.println("ModuleSupport startup fail:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private Class<?> getModuleSupportFactoryClass(ClassLoader parentClassLoader) {
        try {
            return Class.forName("com.navercorp.pinpoint.bootstrap.module.Java9ModuleSupportFactory", false, parentClassLoader);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("ModuleSupportFactory not found Caused by:" + ex.getMessage(), ex);
        }
    }

    private Object newModuleSupportFactory(Class<?> bootStrapClass) {
        try {
            Constructor<?> constructor = bootStrapClass.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ModuleSupportFactory() initialize fail", e);
        }
    }
}
