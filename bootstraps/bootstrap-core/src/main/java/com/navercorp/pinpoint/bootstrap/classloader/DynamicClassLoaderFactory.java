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

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DynamicClassLoaderFactory implements ClassLoaderFactory {

    private final Constructor<? extends ClassLoader> constructor;

    public DynamicClassLoaderFactory(String classLoaderName, ClassLoader classLoader) {
        this.constructor = getConstructor(classLoaderName, classLoader);
    }


    private static Constructor<? extends ClassLoader> getConstructor(String classLoaderName, ClassLoader classLoader) {
        try {
            final Class<? extends ClassLoader> classLoaderClazz =
                    (Class<? extends ClassLoader>) Class.forName(classLoaderName, true, classLoader);
            Constructor<? extends ClassLoader> constructor = classLoaderClazz.getDeclaredConstructor(String.class, URL[].class, ClassLoader.class, List.class);
            return constructor;
        } catch (Exception ex) {
            throw new IllegalStateException(classLoaderName +  " initialize fail Caused by:" + ex.getMessage(), ex);
        }
    }


    @Override
    public ClassLoader createClassLoader(String name, URL[] urls, ClassLoader parent, List<String> libClass) {
        try {
            return constructor.newInstance(name, urls, parent, libClass);
        } catch (Exception ex) {
            throw new IllegalStateException(constructor + " invoke fail Caused by:" + ex.getMessage(), ex);
        }
    }
}
