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

import com.navercorp.pinpoint.bootstrap.agentdir.Assert;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author Woonduk Kang(emeroad)
 */
class ModuleBootLoader {

    private final Instrumentation instrumentation;
    // @Nullable
    private final ClassLoader parentClassLoader;

    private Object moduleSupport;

    ModuleBootLoader(Instrumentation instrumentation, ClassLoader parentClassLoader) {
        this.instrumentation = Assert.requireNonNull(instrumentation, "instrumentation");
        this.parentClassLoader = parentClassLoader;
    }

    void loadModuleSupport() {
        try {
            Class<?> bootStrapClass = getModuleSupportFactoryClass(parentClassLoader);
            Object moduleSupportFactory = newModuleSupportFactory(bootStrapClass);

            Method newModuleSupportMethod = moduleSupportFactory.getClass().getMethod("newModuleSupport", Instrumentation.class);
            this.moduleSupport = newModuleSupportMethod.invoke(moduleSupportFactory, instrumentation);

            Class<?> moduleSupportSetup = moduleSupport.getClass();
            Method setupMethod = moduleSupportSetup.getMethod("setup");
            setupMethod.invoke(moduleSupport);
        } catch (Exception e) {
            throw new IllegalStateException("moduleSupport load fail Caused by:" + e.getMessage(), e);
        }
    }

    void defineAgentModule(ClassLoader classLoader, URL[] jarFileList) {
        if (moduleSupport == null) {
            throw new IllegalStateException("moduleSupport not loaded");
        }
        try {
            Method definePinpointPackage = this.moduleSupport.getClass().getDeclaredMethod("defineAgentModule", ClassLoader.class, URL[].class);
            definePinpointPackage.invoke(moduleSupport, classLoader, jarFileList);
        } catch (Exception ex) {
            throw new IllegalStateException("defineAgentPackage fail: Caused by:" + ex.getMessage(), ex);
        }
    }


    private Class<?> getModuleSupportFactoryClass(ClassLoader parentClassLoader) {
        try {
            return Class.forName("com.navercorp.pinpoint.bootstrap.java9.module.ModuleSupportFactory", false, parentClassLoader);
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
