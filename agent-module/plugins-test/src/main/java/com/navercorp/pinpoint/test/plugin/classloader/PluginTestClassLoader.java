/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.classloader;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsJdkPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsJunitPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsLogPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointCommonPackage;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;

// parent: jdk, log, junit
public class PluginTestClassLoader extends URLClassLoader {
    public static final IsJdkPackage isJdkPackage = new IsJdkPackage();
    public static final IsLogPackage isLogPackage = new IsLogPackage();
    public static final IsJunitPackage isJunitPackage = new IsJunitPackage();
    public static final IsPinpointCommonPackage isPinpointCommonPackage = new IsPinpointCommonPackage();

    // find child first classloader
    static {
        registerAsParallelCapable();
    }

    private String classLoaderName;
    private URL[] urls;

    public PluginTestClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
        setClassLoaderName(getClass().getSimpleName());
    }

    public String getClassLoaderName() {
        return classLoaderName;
    }

    public void setClassLoaderName(String classLoaderName) {
        this.classLoaderName = classLoaderName;
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassFormatError, ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {

            Class<?> c = findLoadedClass(name);
            if (c == null) {
                if (isDelegated(name)) {
                    c = loadClassParentFirst(name, resolve);
                }
            }
            if (c == null) {
                try {
                    c = loadClassChildFirst(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (c == null) {
                throw new ClassNotFoundException("not found " + name + ", urls=" + Arrays.asList(urls));
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    protected boolean isDelegated(String name) {
        return isJdkPackage.test(name) || isLogPackage.test(name) || isJunitPackage.test(name) || isPinpointCommonPackage.test(name);
    }

    protected boolean isChild(String name) {
        return true;
    }

    public Class<?> loadClassParentFirst(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    public Class<?> loadClassChildFirst(final String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public URL getResource(String name) {
        final String className = JavaAssistUtils.jvmNameToJavaName(name);
        if (isDelegated(className)) {
            return super.getResource(name);
        }

        URL url = findResource(name);
        if (url != null) {
            return url;
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final String className = JavaAssistUtils.jvmNameToJavaName(name);
        if (isDelegated(className)) {
            return super.getResources(name);
        }

        return findResources(name);
    }

    public void clear() {
        if (this.urls != null) {
            this.urls = null;
        }
        try {
            close();
        } catch (IOException e) {
        }
    }

    @Override
    public String toString() {
        return "{" +
                "classLoaderName='" + classLoaderName + '\'' +
                ", urls=" + Arrays.toString(urls) +
                '}';
    }
}
