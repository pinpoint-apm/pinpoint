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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class BaseClassLoader extends URLClassLoader {

    protected static final LibClass PROFILER_LIB_CLASS = new ProfilerLibClass();

    //  @Nullable
    // WARNING : if parentClassLoader is null. it is bootstrapClassloader
    private final ClassLoader parent;

    private final LibClass libClass;

    public BaseClassLoader(URL[] urls, ClassLoader parent, LibClass libClass) {
        super(urls, parent);

        if (libClass == null) {
            throw new NullPointerException("libClass must not be null");
        }
        this.parent = parent;
        this.libClass = libClass;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = findBootstrapResource0(name);
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
            parentResource = findBootstrapResources0(name);
        }

        return new MergedEnumeration2<URL>(currentResource, parentResource);
    }

    protected abstract URL findBootstrapResource0(String name);

    protected abstract Enumeration<URL> findBootstrapResources0(String name) throws IOException;

    protected abstract Object getClassLoadingLock0(String name);

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock0(name)) {
            // First, check if the class has already been loaded
            Class clazz = findLoadedClass(name);
            if (clazz == null) {
                if (onLoadClass(name)) {
                    // load a class used for Pinpoint itself by this PinpointClassLoader
                    clazz = findClass(name);
                } else {
                    try {
                        // load a class by parent ClassLoader
                        if (parent != null) {
                            clazz = parent.loadClass(name);
                        } else {
                            clazz = findBootstrapClassOrNull0(this, name);
                        }
                    } catch (ClassNotFoundException ignore) {
                    }
                    if (clazz == null) {
                        // if not found, try to load a class by this PinpointClassLoader
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

    protected abstract Class findBootstrapClassOrNull0(ClassLoader classLoader, String name);

    // for test
    boolean onLoadClass(String name) {
        return libClass.onLoadClass(name);
    }

}
