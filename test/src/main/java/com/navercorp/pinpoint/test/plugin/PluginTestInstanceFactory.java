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

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.test.MockApplicationContextFactory;
import com.navercorp.pinpoint.test.PluginVerifierExternalAdaptor;
import com.navercorp.pinpoint.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.test.classloader.TestClassLoaderFactory;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PluginTestInstanceFactory {

    private final PluginTestContext context;

    public PluginTestInstanceFactory(PluginTestContext context) {
        this.context = context;
    }

    public PluginTestInstance create(String testId, List<String> libs, boolean onSystemClassLoader) throws ClassNotFoundException {
        final String id = testId + ":" + (onSystemClassLoader ? "system" : "child") + ":" + context.getJvmVersion();
        final MockApplicationContextFactory factory = new MockApplicationContextFactory();
        final DefaultApplicationContext applicationContext = factory.build(context.getConfigFile());
//        final InterceptorRegistryBinder interceptorRegistryBinder = applicationContext.getInterceptorRegistryBinder();
        final InterceptorRegistryBinder interceptorRegistryBinder = null;
        final PluginTestVerifier pluginVerifier = new PluginVerifierExternalAdaptor(applicationContext);

        try {
//            interceptorRegistryBinder.bind();
            final List<File> fileList = new ArrayList<>();
            for (String classPath : getClassPath(libs, onSystemClassLoader)) {
                File file = new File(classPath);
                fileList.add(file);
            }
            final URL[] urls = URLUtils.fileToUrls(fileList);
            final TestClassLoader classLoader = TestClassLoaderFactory.createTestClassLoader(applicationContext);
            classLoader.initialize();

            final Class<?> testClass = classLoader.loadClass(context.getTestClass().getName());
            return new DefaultPluginTestInstance(id, classLoader, testClass, pluginVerifier, interceptorRegistryBinder);
        } finally {
            interceptorRegistryBinder.unbind();
        }
    }

    List<String> getClassPath(List<String> libs, boolean onSystemClassLoader) {
        final List<String> libList = new ArrayList<>(context.getRequiredLibraries());
        libList.addAll(libs);
        libList.add(context.getTestClassLocation());
        return libList;
    }
}
