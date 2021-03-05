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

package com.navercorp.pinpoint.bootstrap.classloader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

/**
 * for ibm j9 jvm
 *
 * @author Woonduk Kang(emeroad)
 */
public class J9BootLoader implements BootLoader {

    private static final ClassLoader bootstrapClassLoader ;
    private static final Method findResource;
    private static final Method findResources;
    private static final boolean isJ9VM;

    static {
        bootstrapClassLoader = findBootstrapClassLoader();
        final Class<?> abstractClassLoader = getAbstractClassLoader("com.ibm.oti.vm.AbstractClassLoader");
        findResource = getMethod(abstractClassLoader, "findResource", String.class);
        findResources = getMethod(abstractClassLoader, "findResources", String.class);
        isJ9VM = true;
    }

    private static ClassLoader findBootstrapClassLoader() {
        Field bootstrapClassLoader;
        try {
            bootstrapClassLoader = ClassLoader.class.getDeclaredField("bootstrapClassLoader");
        } catch (NoSuchFieldException e) {
            try {
                bootstrapClassLoader = ClassLoader.class.getDeclaredField("systemClassLoader");
            } catch (NoSuchFieldException e2) {
                throw new IllegalStateException("bootstrapClassLoader not found Caused by:" + e.getMessage() + ", and " + e2.getMessage(), e2);
            }
        }
        try {
            bootstrapClassLoader.setAccessible(true);
            return (ClassLoader) bootstrapClassLoader.get(ClassLoader.class);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("bootstrapClassLoader access fail Caused by:" + e.getMessage(), e);
        }
    }

    private static Class<?> getAbstractClassLoader(String className) {
        try {
            return Class.forName(className, false, Object.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(className + " not found Caused by:" + e.getMessage(), e);
        }
    }


    private static Method getMethod(Class<?> bootstrapClassLoader, String methodName, Class<?>... parameterTypes) {
        try {
            Method declaredMethod = bootstrapClassLoader.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(methodName + " not found Caused by:" + e.getMessage(), e);
        }

    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        try {
            return (Enumeration<URL>) findResources.invoke(bootstrapClassLoader, name);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("access fail Caused by:" + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("invoke fail Caused by:" + e.getMessage(), e);
        }
    }

    @Override
    public URL findResource(String name) {
        try {
            return (URL) findResource.invoke(bootstrapClassLoader, name);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("access fail Caused by:" + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("invoke fail Caused by:" + e.getMessage(), e);
        }
    }

    @Override
    public Class<?> findBootstrapClassOrNull(ClassLoader classLoader, String name) {
        try {
            return bootstrapClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
