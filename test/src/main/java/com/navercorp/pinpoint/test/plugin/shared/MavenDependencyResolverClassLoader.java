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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<URL> urlList = getUrlList(jars);

        return new MavenDependencyResolverClassLoader(urlList.toArray(new URL[0]));
    }

    private static List<URL> getUrlList(String[] jars) {
        if (jars == null) {
            return Collections.emptyList();
        }

        List<URL> urlList = new ArrayList<URL>();
        for (String jar : jars) {
            URL url = toURL(jar);
            urlList.add(url);
        }
        return urlList;
    }

    private static URL toURL(String jar) {
        try {
            File file = new File(jar);
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
