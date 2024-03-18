/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.instrument.BootstrapPackage;

import java.io.InputStream;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DebugTransformerClassInjector implements ClassInjector {

    private static final BootstrapPackage bootstrapPackage = new BootstrapPackage();

    public DebugTransformerClassInjector() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        ClassLoader targetClassLoader = classLoader;
        if (bootstrapPackage.isBootstrapPackage(className)) {
            targetClassLoader = Object.class.getClassLoader();
        }

        try {
            return (Class<? extends T>) Class.forName(className, false, targetClassLoader);
        } catch (ClassNotFoundException e) {
            throw new PinpointException("ClassNo class " + className + " with classLoader " + classLoader, e);
        }
    }


    @Override
    public InputStream getResourceAsStream(ClassLoader classLoader, String internalName) {
        ClassLoader targetClassLoader = getClassLoader(classLoader);

        targetClassLoader = filterBootstrapPackage(targetClassLoader, internalName);

        return targetClassLoader.getResourceAsStream(internalName);
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }


    private ClassLoader filterBootstrapPackage(ClassLoader classLoader, String classPath) {
        if (bootstrapPackage.isBootstrapPackageByInternalName(classPath)) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }
}
