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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.util.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class MavenDependencyResolverClassLoader extends URLClassLoader {
    // find child first classloader

    public MavenDependencyResolverClassLoader(URL[] urls) {
        super(urls);
    }

    public MavenDependencyResolverClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return findClass(name);
        } catch (ClassNotFoundException e) {
        }

        return super.loadClass(name);
    }

    static ClassLoader getClassLoader(String[] jars) {
        final URL[] urlList = getUrlList(jars);

        return new MavenDependencyResolverClassLoader(urlList);
    }

    private static URL[] getUrlList(String[] jars) {
        if (jars == null) {
            return new URL[0];
        }

        try {
            return FileUtils.toURLs(jars);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
