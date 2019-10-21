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

package com.navercorp.pinpoint.bootstrap.java9.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;

public class Java9ClassLoader extends URLClassLoader {

    static {
        if (!ClassLoader.registerAsParallelCapable()) {
            throw new IllegalStateException("registerAsParallelCapable() fail");
        }
    }

    private final Java9BootLoader bootLoader = new Java9BootLoader();
    //  @Nullable
    // WARNING : if parentClassLoader is null. it is bootstrapClassloader
    private final ClassLoader parent;
    private final ProfilerLibClass profilerLibClass;

    public Java9ClassLoader(String name, URL[] urls, ClassLoader parent, List<String> libClass) {
        super(name, urls, parent);
        this.parent = parent;

        if (libClass == null) {
            throw new NullPointerException("profilerLibClass");
        }
        this.profilerLibClass = new ProfilerLibClass(libClass);
    }

    @Override
    protected URL findResource(String moduleName, String name) throws IOException {
        if (getName().equals(moduleName)) {
            return findResource(name);
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    private Object getClassLoadingLock0(String name) {
        return getClassLoadingLock(name);
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = bootLoader.findResource(name);
            }
        }

        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final Enumeration<URL> currentResource = findResources(name);

        Enumeration<URL> parentResource;
        if (parent != null) {
            parentResource = parent.getResources(name);
        } else {
            parentResource = bootLoader.findResources(name);
        }

        return new MergedEnumeration2<>(currentResource, parentResource);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock0(name)) {
            // First, check if the class has already been loaded
            Class clazz = findLoadedClass(name);
            if (clazz == null) {
                if (onLoadClass(name)) {
                    // load a class used for Pinpoint itself by this ClassLoader
                    clazz = findClass(name);
                } else {
                    try {
                        // load a class by parent ClassLoader
                        if (parent != null) {
                            clazz = parent.loadClass(name);
                        } else {
                            clazz = bootLoader.findBootstrapClassOrNull(this, name);
                        }
                    } catch (ClassNotFoundException ignore) {
                    }
                    if (clazz == null) {
                        // if not found, try to load a class by this ClassLoader
                        clazz = findClass(name);
                    }
                }
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    // for test
    private boolean onLoadClass(String name) {
        return profilerLibClass.onLoadClass(name);
    }

    @Override
    public String toString() {
        return "Java9ClassLoader{" +
                "name=" + getName() +
                "} " + super.toString();
    }
}
