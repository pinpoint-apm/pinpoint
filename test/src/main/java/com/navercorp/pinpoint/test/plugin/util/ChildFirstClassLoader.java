/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

public class ChildFirstClassLoader extends URLClassLoader {
    // find child first classloader
    static {
        registerAsParallelCapable();
    }

    private final ParentClass parentClass;

    public ChildFirstClassLoader(URL[] urls) {
        super(urls);
        this.parentClass = new ParentClass(Collections.<String>emptyList());
    }

    public ChildFirstClassLoader(URL[] urls, List<String> delegateParent) {
        super(urls);
        this.parentClass = new ParentClass(delegateParent);
    }

    public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parentClass = new ParentClass(Collections.<String>emptyList());
    }

    public ChildFirstClassLoader(URL[] urls, ClassLoader parent, List<String> delegateParent) {
        super(urls, parent);
        this.parentClass = new ParentClass(delegateParent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                if (parentClass.onDelegate(name)) {
                    return super.loadClass(name, resolve);
                } else {
                    try {
                        loadedClass = findClass(name);
                    } catch (ClassNotFoundException ignore) {
                        // ignore
                    }
                    if (loadedClass == null) {
                        loadedClass = super.loadClass(name, resolve);
                    }
                }
            }
            if (resolve) {
                resolveClass(loadedClass);
            }
            return loadedClass;
        }
    }

}
