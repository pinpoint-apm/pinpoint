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
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsTransformInclude;

import java.net.URL;
import java.util.List;
import java.util.function.Predicate;

// parent: "java...", "javax...", "com.navercorp.pinpoint.test.", "com.navercorp.pinpoint.bootstrap.plugin.test."
// this: "com.navercorp.pinpoint.profiler.test.", "com.navercorp.pinpoint.test.plugin.agent."
public class PluginAgentTestClassLoader extends PluginTestClassLoader {
    public static final Predicate<String> isPinpointPackage = new IsPinpointPackage();
    public static final Predicate<String> isPinpointTestPackage = new IsPinpointTestPackage();
    public static final Predicate<String> isPinpointTestAgentPackage = new IsPinpointTestAgentPackage();
    public static final Predicate<String> isPinpointBootstrapPluginTestPackage = new IsPinpointBootstrapPluginTestPackage();

    private final Predicate<String> isTransformInclude;

    // set once the agent has started: the test class loader needs the agent's callback, so it cannot exist yet
    private PluginTestJunitTestClassLoader testClassLoader;

    public PluginAgentTestClassLoader(URL[] urls, ClassLoader parent, List<String> transformIncludeList) {
        super(urls, parent);
        this.isTransformInclude = new IsTransformInclude(transformIncludeList);
        setClassLoaderName(getClass().getSimpleName());
    }

    public void setTestClassLoader(PluginTestJunitTestClassLoader testClassLoader) {
        this.testClassLoader = testClassLoader;
    }

    @Override
    public boolean isDelegated(String name) {
        if (isTransformInclude.test(name)) {
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
        if (testClassLoader != null && !isPinpointPackage.test(name)) {
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
            if (testClassLoader != null && !isPinpointPackage.test(name)) {
                c = testClassLoader.loadClass(name, false);
            }
        }
        return c;
    }

}
