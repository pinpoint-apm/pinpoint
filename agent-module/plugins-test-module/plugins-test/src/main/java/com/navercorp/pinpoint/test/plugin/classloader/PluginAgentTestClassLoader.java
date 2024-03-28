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

package com.navercorp.pinpoint.test.plugin.classloader;

import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointBootstrapPluginTestPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointTestAgentPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointTestPackage;

import java.net.URL;
import java.util.List;

// parent: "java...", "javax...", "com.navercorp.pinpoint.test.", "com.navercorp.pinpoint.bootstrap.plugin.test."
// this: "com.navercorp.pinpoint.profiler.test.", "com.navercorp.pinpoint.test.plugin.agent."
public class PluginAgentTestClassLoader extends PluginTestClassLoader {
    public static final IsPinpointPackage isPinpointPackage = new IsPinpointPackage();
    public static final IsPinpointTestPackage isPinpointTestPackage = new IsPinpointTestPackage();
    public static final IsPinpointTestAgentPackage isPinpointTestAgentPackage = new IsPinpointTestAgentPackage();
    public static final IsPinpointBootstrapPluginTestPackage isPinpointBootstrapPluginTestPackage = new IsPinpointBootstrapPluginTestPackage();

    private PluginTestJunitTestClassLoader testClassLoader;
    private List<String> transformIncludeList;

    public PluginAgentTestClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        setClassLoaderName(getClass().getSimpleName());
    }

    public void setTestClassLoader(PluginTestJunitTestClassLoader testClassLoader) {
        this.testClassLoader = testClassLoader;
    }

    public void setTransformIncludeList(List<String> transformIncludeList) {
        this.transformIncludeList = transformIncludeList;
    }

    @Override
    public boolean isDelegated(String name) {
        if (isTransformInclude(name)) {
            return false;
        }

        if (isPinpointTestAgentPackage.test(name)) {
            return false;
        }
        return super.isDelegated(name) || isPinpointTestPackage.test(name) || isPinpointBootstrapPluginTestPackage.test(name);
    }

    @Override
    public Class<?> loadClassChildFirst(String name) throws ClassNotFoundException {
        // Find provided class
        if (testClassLoader != null && Boolean.FALSE == isPinpointPackage.test(name)) {
            if (testClassLoader.isLoadedClass(name)) {
                return testClassLoader.loadClass(name, false);
            }
        }

        Class<?> c = null;
        try {
            c = findClass(name);
        } catch (ClassNotFoundException ignored) {
        }

        if (c == null) {
            if (testClassLoader != null && Boolean.FALSE == isPinpointPackage.test(name)) {
                c = testClassLoader.loadClass(name, false);
            }
        }
        return c;
    }

    boolean isTransformInclude(String name) {
        if (transformIncludeList != null) {
            for (String transformInclude : transformIncludeList) {
                if (transformInclude.endsWith(".")) {
                    if (name.startsWith(transformInclude)) {
                        return true;
                    }
                } else {
                    if (name.equals(transformInclude)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
