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

package com.navercorp.pinpoint.bootstrap.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;

/**
 * this ClassLoader loads a class in the profiler lib directory and delegates to load the other classes to parent classloader
 * Dead lock could happen in case of standalone java application.
 * Don't delegate to parents classlaoder if classes are in the profiler lib directory
 *
 * @author emeroad
 */
public class Java6ClassLoader extends URLClassLoader {

    private final BootLoader bootLoader = BootLoaderFactory.newBootLoader();
    //  @Nullable
    // WARNING : if parentClassLoader is null. it is bootstrapClassloader
    private final ClassLoader parent;
    private final LibClass libClass;
    private final String name;

    public Java6ClassLoader(String name, URL[] urls, ClassLoader parent, List<String> libClass) {
        super(urls, parent);
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;

        if (libClass == null) {
            throw new NullPointerException("libClass");
        }
        this.parent = parent;
        this.libClass = new ProfilerLibClass(libClass);
    }


    private Object getClassLoadingLock0(String name) {
        return this;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = this.bootLoader.findResource(name);
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
            parentResource = this.bootLoader.findResources(name);
        }

        return new MergedEnumeration2<URL>(currentResource, parentResource);
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
                            clazz = this.bootLoader.findBootstrapClassOrNull(this, name);
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
        return libClass.onLoadClass(name);
    }

    /**
     * java9 JPMS
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Java6ClassLoader{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
