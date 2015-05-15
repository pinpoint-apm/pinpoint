/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.plugin;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

import sun.misc.Unsafe;

/**
 * @author Jongho Moon
 * @author emeroad
 */
@SuppressWarnings("restriction")
public class Java6UnsafePluginClassLoader extends URLClassLoader {

    private static final Unsafe unsafe;
    
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe)f.get(null);
            
            // Ensure required classes are loaded
            while (!unsafe.tryMonitorEnter(Java6UnsafePluginClassLoader.class)) { }
            Java6UnsafePluginClassLoader.class.wait(0, 1);
            unsafe.monitorExit(Java6UnsafePluginClassLoader.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get unsafe", e);
        }
    }
    
    private final ClassLoader parent;

    public Java6UnsafePluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;

        synchronized (this) {
            c = findLoadedClass(name);

            if (c == null) {
                try {
                    while (!unsafe.tryMonitorEnter(parent)) {
                        try {
                            this.wait(0, 1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted", e);
                        }
                    }
                    
                    try {
                        c = parent.loadClass(name);
                    } finally {
                        unsafe.monitorExit(parent);
                    }
                    
                } catch (ClassNotFoundException e) {

                }

                if (c == null) {
                    c = findClass(name);
                }
            }

            if (resolve) {
                resolveClass(c);
            }

            this.notify();
        }

        return c;
    }
}
